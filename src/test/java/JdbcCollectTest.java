import com.lfc.agent.jdbc.MySqlDriverAgent;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import com.mysql.cj.jdbc.StatementImpl;
import com.mysql.cj.jdbc.result.ResultSetImpl;
import javassist.ClassPool;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;

import java.sql.*;

public class JdbcCollectTest {

	@Test
	public void buildTest() throws Exception {
//		Class<NonRegisteringDriver> nonRegisteringDriverClass = NonRegisteringDriver.class;

	}
	@Test
	public void sqlTest() throws Exception {

	}
	@Test
	public void testBatch() throws Exception {
		buildTest();
		for (int i = 0; i < 130; i++) {
			sqlTest();
		}
	}

	public static void main(String[] args) throws Exception {
		//3. 获取数据库连接对象 Connection
		Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/wgw2","root","123456");
		//4. 定义 sql
		//3、 sql语句
		String sql="select * from  sys_user where user_id=? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1,1);
		//
		/*ResultSetImpl resultSet = (ResultSetImpl) pstmt.executeQuery();
		ClientPreparedStatement owningStatement = (ClientPreparedStatement) resultSet.getOwningStatement();
		owningStatement.asSql();*/
		boolean execute = ps.execute();
		conn.close();
	}
}