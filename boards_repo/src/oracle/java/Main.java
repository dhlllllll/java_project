package oracle.sec13;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
		//Field
		private Scanner scanner = new Scanner(System.in);
		private Connection conn;
		private String loginId;
		
		//Constructor
		private Main() {
			try {
				//JDBC 등록
				Class.forName("oracle.jdbc.OracleDriver");
				
				//연결하기
				conn = DriverManager.getConnection(
						"jdbc:oracle:thin:@localhost:1521/xe",
						"java",
						"oracle"
						);
			} catch (Exception e) {
				e.printStackTrace();
				exit();
			}
		}

		//Method
		public void list() {
			//게시물 목록을 출력하고 mainMenu()메소드를 호출
			System.out.println();
			System.out.println("[게시물 목록] " + ((loginId!=null) ? ("사용자: "+ loginId) : ""));
			System.out.println("--------------------------------------------------------");
			System.out.printf("%-6s%-12s%-16s%-40s\n","no","writer","date","title");
			System.out.println("--------------------------------------------------------");
			
			//boards 테이블에서 게시물 정보를 가져와서 출력하기
			try {
				String sql = "" +
						"SELECT bno, btitle, bcontent, bwriter, bdate " + 
						"FROM boards " + 
						"ORDER BY bno DESC";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery();
				while(rs.next()) {
					Board board = new Board();
					board.setBno(rs.getInt("bno"));
					board.setBtitle(rs.getString("btitle"));
					board.setBcontent(rs.getString("bcontent"));
					board.setBwriter(rs.getString("bwriter"));
					board.setBdate(rs.getDate("bdate"));
					System.out.printf("%-6s%-12s%-16s%-40s \n",
							board.getBno(),
							board.getBwriter(),
							board.getBdate(),
							board.getBtitle());
				}
				rs.close();
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
				exit();
			}

			mainMenu();
		}
		

		private void mainMenu() {
			System.out.println();
			System.out.println("--------------------------------------------------------");
			if(loginId==null) {
				System.out.println("메인 메뉴: 1.Create | 2.Read | 3.Clear | 4.Join | 5.Login | 6.Exit");
				System.out.print("메뉴 선택: ");
				String menuNo = scanner.nextLine();
				System.out.println();
				
				switch(menuNo) {
				case "1" -> create();
				case "2" -> read();
				case "3" -> clear();
				case "4" -> join();
				case "5" -> login();
				case "6" -> exit();
				}				
			} else {
				System.out.println("메인 메뉴: 1.Create | 2.Read | 3.Clear | 4.Logout | 5.Exit");
				System.out.print("메뉴 선택: ");
				String menuNo = scanner.nextLine();
				System.out.println();
				
				switch(menuNo) {
				case "1" -> create();
				case "2" -> read();
				case "3" -> clear();
				case "4" -> logout();
				case "5" -> exit();
				}	
			}
		}
		
		public void login() {
			//입력받기 
			User user = new User();
			System.out.println("[로그인]");
			System.out.print("아이디: ");
			user.setUserId(scanner.nextLine());
			System.out.print("비밀번호: ");
			user.setUserPassword(scanner.nextLine());
			
			//보조 메뉴 출력
			System.out.println("--------------------------------------------------------");
			System.out.println("보조 메뉴: 1.ok | 2.Cancel");
			System.out.print("메뉴 선택: ");
			String menuNo = scanner.nextLine();
			
			if(menuNo.equals("1")) {
				try {
					//sql문 작성 
					String sql = "" + 
							"SELECT userpassword " + 
							"FROM users " + 
							"WHERE userid = ?";
					//prepareStatement 얻기 및 값 지정
					PreparedStatement pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, user.getUserId());
					//SQL문 실행 후, ResultSet을 통해 데이터 읽기
					ResultSet rs = pstmt.executeQuery();
					if(rs.next()) {
						String dbPassword = rs.getString("userpassword");
						if(dbPassword.equals(user.getUserPassword())) {
							loginId = user.getUserId();
						} else {
							System.out.println("비밀번호가 일치하지않습니다. \n");
							login();
						}
					} else { //db에서 일치해서 가져온 password값이 없으므로 
						System.out.println("아이디가 존재하지 않습니다. \n");
						login();
					}
					rs.close();
					pstmt.close();
				} catch (Exception e) {
					e.printStackTrace();
					exit();
				}
			}
			//게시물 목록 출력
			list();
		}
		
		public void logout() {
			//로그인 아이디 없애기
			loginId = null;
			
			//게시물 목록 출력
			list();
		}
		
		public void join() {
			//입력받기 
			User user = new User();
			System.out.println("[새 사용자 입력]");
			System.out.print("아이디: ");
			user.setUserId(scanner.nextLine());
			System.out.print("이름: ");
			user.setUserName(scanner.nextLine());
			System.out.print("비밀번호: ");
			user.setUserPassword(scanner.nextLine());
			System.out.print("나이: ");
			user.setUserAge(Integer.parseInt(scanner.nextLine()));
			System.out.print("이메일: ");
			user.setUserEmail(scanner.nextLine());
			
			
			//보조 메뉴 출력
			System.out.println("--------------------------------------------------------");
			System.out.println("보조 메뉴: 1.ok | 2.Cancel");
			System.out.print("메뉴 선택: ");
			String menuNo = scanner.nextLine();
			if(menuNo.equals("1")) {
				//user 테이블에 개인 정보 저장
				try {
					String sql = "" + 
							"INSERT INTO users (userid, username, userpassword, userage, useremail) " +
							"VALUES (?,?,?,?,?)";
					PreparedStatement pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, user.getUserId());
					pstmt.setString(2, user.getUserName());
					pstmt.setString(3, user.getUserPassword());
					pstmt.setInt(4, user.getUserAge());
					pstmt.setString(5, user.getUserEmail());
					pstmt.executeUpdate();
					pstmt.close();
				} catch (Exception e) {
					e.printStackTrace();
					exit();
				}
			}
			//게시물 목록 다시 출력 
			list();
		}


		private void create() {
			//입력받기
			Board board = new Board();
			System.out.println("[새 게시물 입력]");
			System.out.print("제목: ");
			board.setBtitle(scanner.nextLine());
			System.out.print("내용: ");
			board.setBcontent(scanner.nextLine());
			if(loginId==null) {
				System.out.println("작성자: ");
				board.setBwriter(scanner.nextLine());
			} else { //로그인 한 상태이면 
				board.setBwriter(loginId);
			}
			
			//보조 메뉴 출력
			System.out.println("--------------------------------------------------------");
			System.out.println("보조 메뉴: 1.ok | 2.Cancel");
			System.out.print("메뉴 선택: ");
			String menuNo = scanner.nextLine();
			if(menuNo.equals("1")) {
				//boards 테이블에 게시물 저장
				try {
					String sql = "" + 
							"INSERT INTO boards (bno, btitle, bcontent, bwriter, bdate) " +
							"VALUES (SEQ_BNO.NEXTVAL, ? ,?, ?, SYSDATE)";
					PreparedStatement pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, board.getBtitle());
					pstmt.setString(2, board.getBcontent());
					pstmt.setString(3, board.getBwriter());
					pstmt.executeUpdate();
					pstmt.close();
				} catch (Exception e) {
					e.printStackTrace();
					exit();
				}
			}
			//게시물 목록 출력
			list();
		}
		
		private void read() {
			//입력받기
			System.out.println("[게시물 읽기]");
			System.out.print("bno: ");
			int bno = Integer.parseInt(scanner.nextLine());
			
			//boards 테이블에서 해당 게시물을 가져와 출력
			try {
				String sql = "" +
						"SELECT bno, btitle, bcontent, bwriter, bdate " +
						"FROM boards " +
						"WHERE bno=?";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, bno);
				ResultSet rs = pstmt.executeQuery();
				if(rs.next()) {
					Board board = new Board();
					board.setBno(rs.getInt("bno"));
					board.setBtitle(rs.getString("btitle"));
					board.setBcontent(rs.getString("bcontent"));
					board.setBwriter(rs.getString("bwriter"));
					board.setBdate(rs.getDate("bdate"));
					System.out.println("##########");
					System.out.println("번호: " + board.getBno());
					System.out.println("제목: " + board.getBtitle());
					System.out.println("내용: " + board.getBcontent());
					System.out.println("작성자: " + board.getBwriter());
					System.out.println("날짜: " + board.getBdate());
					
					//로그인 한 상태이며, 로그인 한 아이디가 글쓴이 아이디랑 동일하다면
					if(loginId!=null && loginId.equals(board.getBwriter())) {
						//보조 메뉴 출력
						System.out.println("----------------");
						System.out.println("보조 메뉴: 1.Update | 2.Delete | 3.List");
						System.out.print("메뉴 선택: ");
						String menuNo = scanner.nextLine();
						System.out.println();
						
						if(menuNo.equals("1")) {
							update(board);
						} else if(menuNo.equals("2")) {
							delete(board);
						}						
					}					
				}
				rs.close();
				pstmt.close();
			} catch (Exception e) {
				e.printStackTrace();
				exit();
			}			
			//게시물 목록 출력
			list();
		}
		
		private void update(Board board) {
			//수정 내용 입력 받기 
			System.out.println("[수정 내용 입력]");
			System.out.print("제목: ");
			board.setBtitle(scanner.nextLine());
			System.out.print("내용: ");
			board.setBcontent(scanner.nextLine());
			
			//보조 메뉴 출력
			System.out.println("--------------------------------------------------------");
			System.out.println("보조 메뉴: 1.ok | 2.Cancel");
			System.out.print("메뉴 선택: ");
			String menuNo = scanner.nextLine();
			if(menuNo.equals("1")) {
				//boards 테이블에서 게시물 정보 수정
				try {
					String sql = "" +
							"UPDATE boards SET btitle=?, bcontent=? " + 
							"WHERE bno=?";
					PreparedStatement pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, board.getBtitle());
					pstmt.setString(2, board.getBcontent());
					pstmt.setInt(3, board.getBno());
					pstmt.executeUpdate();
					pstmt.close();
				} catch (Exception e) {
					e.printStackTrace();
					exit();
				}
			}
			//게시물 목록 출력
			list();
		}
		
		public void delete(Board board) {
			//boards 테이블에 게시물 정보 삭제
			try {
				String sql = "DELETE FROM boards WHERE bno=?";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, board.getBno());
				pstmt.executeUpdate();
				pstmt.close();
			} catch (Exception e) {
				e.printStackTrace();
				exit();
			}
			//게시물 목록 출력
			list();
		}
		
		private void clear() {
			System.out.println("[게시물 전체 삭제]");
			System.out.println("--------------------------------------------------------");
			System.out.println("보조 메뉴: 1.ok | 2.Cancel");
			System.out.print("메뉴 선택: ");
			String menuNo = scanner.nextLine();
			if(menuNo.equals("1")) {
				//boards 테이블 게시물 정보 전체 삭제
				try {
					String sql = "TRUNCATE TABLE boards";
					PreparedStatement pstmt = conn.prepareStatement(sql);
					pstmt.executeUpdate();
					pstmt.close();
				} catch (Exception e) {
					e.printStackTrace();
					exit();
				}
			}
			//게시물 목록 출력
			list();
		}
		private void exit() {
			if(conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
			System.out.println("** 게시판 종료 **");
			System.exit(0);
		}

	public static void main(String[] args) {
		Main boardExample = new Main();
		boardExample.list();
	}

}
