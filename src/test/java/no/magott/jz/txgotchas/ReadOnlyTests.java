package no.magott.jz.txgotchas;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:txgotchas-readonly-context.xml")
@TransactionConfiguration(defaultRollback=true)
public class ReadOnlyTests {
	
	private static Logger log = LogManager.getLogger(ReadOnlyTests.class);

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	@Qualifier(value="createTableStatement")
	private String createTableStatement;
	
	@Test
	@Transactional(propagation=Propagation.REQUIRED, readOnly=true)	
	public void attemptInsertWithReadOnlyTx(){
		log.debug("Executing create table");
		new JdbcTemplate(dataSource).execute(createTableStatement);
		log.info("Executed create table without exceptions");
		
		log.debug("Attempting insert in created table");
		SimpleJdbcInsert personInsert = new SimpleJdbcInsert(dataSource).withTableName("person");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", 1);
		params.put("name", "Morten");
		personInsert.execute(params);
		
		int rowCount = new SimpleJdbcTemplate(dataSource).queryForInt("SELECT COUNT(*) FROM person");
		log.info("Number of row in created table after insert: "+rowCount);
	}
	
	@AfterTransaction
	public void deleteTable(){
		try{
			new JdbcTemplate(dataSource).execute("drop table person");
		}catch(Exception e){
			//Ignore exceptions, table creation might have been rolled back in unit test
			//but some dbs do ddls in seperate txs, so try to delete her just in case
		}
	}
	
}
