package no.magott.jz.txgotchas;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:txgotchas-readonly-context.xml")
public class ReadOnlyInPropagationDefinitionTest {
	

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	@Qualifier(value="createTableStatement")
	private String createTableStatement;
	
	@Autowired
	private RequiredReadOnly requiredReadOnly;
	
	@Test
	@Transactional(propagation=Propagation.REQUIRED)	
	public void attemptInsertWithReadOnlyInDownstreamTxDefinition(){
		
		requiredReadOnly.doCreateTableAndInsertInReadOnly(createTableStatement, dataSource);
		
		System.out.println(new SimpleJdbcTemplate(dataSource).queryForInt("SELECT COUNT(*) FROM person"));
	}
	
	public static class RequiredReadOnly{	
		
		@Transactional(propagation=Propagation.REQUIRED, readOnly=true)
		public void doCreateTableAndInsertInReadOnly(String createTableStatement, DataSource dataSource){
			new JdbcTemplate(dataSource).execute(createTableStatement);
			
			SimpleJdbcInsert personInsert = new SimpleJdbcInsert(dataSource).withTableName("person");
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", 1);
			params.put("name", "Morten");
			personInsert.execute(params);
		}
		
	}
	
}
