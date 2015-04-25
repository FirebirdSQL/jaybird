package org.firebirdsql.hibernate;

import java.io.File;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class PersonHandler {

    public static void main(String[] args) throws Exception {
        File databaseFile = new File("d:/database/jdbc90.fdb");
    String databaseFilename = databaseFile.getAbsolutePath();
        System.out.println("FileName = " + databaseFilename);
        
        //Configuration
        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.dialect","org.hibernate.dialect.FirebirdDialect");
        cfg.setProperty("hibernate.show_sql","true");
        cfg.setProperty("hibernate.format_sql","true");
        cfg.setProperty("hibernate.use_sql_comments","true");
        cfg.setProperty("hibernate.hbm2ddl.auto","create");
        cfg.setProperty("hibernate.current_session_context_class", "thread");       
        cfg.setProperty("hibernate.cglib.use_reflection_optimizer", "false");
        
        //Conection
        cfg.setProperty("hibernate.connection.driver_class",
                "org.firebirdsql.jdbc.FBDriver");
//        cfg.setProperty("hibernate.connection.url","jdbc:firebirdsql:embedded:"
//                + databaseFilename);
        cfg.setProperty("hibernate.connection.url","jdbc:firebirdsql:localhost:"
            + databaseFilename);
        cfg.setProperty("hibernate.connection.username", "SYSDBA");
        cfg.setProperty("hibernate.connection.password", "masterkey");
        
        //Mappings
        cfg.addResource("org/firebirdsql/hibernate/Person.hbm.xml");
        
        //script sql
        SchemaExport schema = new SchemaExport(cfg);
        schema.setOutputFile("schema.sql");
        schema.setDelimiter(";");
        schema.setFormat(true);
        schema.create(true,false);
        
        // Database connection
        SessionFactory sf = cfg.buildSessionFactory();
        Session session = sf.openSession();
        
        // Save data
        Person person = new Person();
        person.setId(new Integer(1));
        person.setName("abcdefghij");
        person.setSurname("");
        session.save(person);
        
        // Criteria
        session.beginTransaction();
   
        Criteria criteria = session.createCriteria(Person.class);
        criteria.add(Restrictions.like("name", "abcdefghi",
            MatchMode.ANYWHERE));
        List result = criteria.list();
        System.out.println("Found: " + result.size());
        
        session.getTransaction().commit();
        
        // Close connection
        session.close();
    }
}
