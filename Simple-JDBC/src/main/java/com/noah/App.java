package com.noah;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.postgresql.ds.PGSimpleDataSource;

import com.noah.dao.BaseDAO;
import com.noah.domain.Address;

public class App {

	public static void main(String[] args) throws Exception {

		Properties dbProperties = new Properties();
		dbProperties.load(App.class.getResourceAsStream("/db.properties"));
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName(dbProperties.getProperty("db.server"));
		dataSource.setDatabaseName(dbProperties.getProperty("db.name"));
		dataSource.setPortNumber(Integer.valueOf(dbProperties.getProperty("db.port")));
		dataSource.setUser(dbProperties.getProperty("db.username"));
		dataSource.setPassword(dbProperties.getProperty("db.password"));

		BaseDAO baseDAO = new BaseDAO(dataSource);

		System.out.println("executeQuery...");
		queryAll(baseDAO).stream().forEach(System.out::println);

		System.out.println("executeQueryWithArgs...");
		System.out.println(queryById(baseDAO));

		System.out.println("executeUpdate...");
		update(baseDAO);
		queryAll(baseDAO).stream().forEach(System.out::println);
		
		System.out.println("executeTransaction...");
		insert(baseDAO);
		queryAll(baseDAO).stream().forEach(System.out::println);

	}

	private static List<Address> queryAll(BaseDAO baseDAO) throws Exception {
		List<Address> addressesReturned = baseDAO
				.executeQuery("select addr_id as addrId, city, street, zip_code as zipCode from address", (rs) -> {
					List<Address> addresses = new ArrayList<>();
					while (rs.next()) {
						Address address = new Address();
						address.setAddrId(rs.getInt("addrId"));
						address.setCity(rs.getString("city"));
						address.setStreet(rs.getString("street"));
						address.setZipCode(rs.getString("zipCode"));
						addresses.add(address);
					}
					return addresses;
				});
		return addressesReturned;
	}

	private static Address queryById(BaseDAO baseDAO) throws Exception {
		Address addressReturned = baseDAO.executeQueryWithArgs(
				"select addr_id as addrId, city, street, zip_code as zipCode from address where addr_id = ?", (rs) -> {
					if (rs.next()) {
						Address address = new Address();
						address.setAddrId(rs.getInt("addrId"));
						address.setCity(rs.getString("city"));
						address.setStreet(rs.getString("street"));
						address.setZipCode(rs.getString("zipCode"));
						return address;
					}
					return null;
				} , 1);
		return addressReturned;
	}

	private static int update(BaseDAO baseDAO) throws Exception {
		return baseDAO.executeUpdate("update address set zip_code = ? where addr_id = ?", "new zip code!!", 3);
	}

	private static int insert(BaseDAO baseDAO) throws Exception {
		return baseDAO.executeTransaction((connection) -> {
			int i = baseDAO.executeUpdateInTransaction(connection,
					"insert into address (city, street, zip_code) values (?,?,?)", "YF", "Golden Street", "5**2*0");
			int k = baseDAO.executeUpdateInTransaction(connection,
					"insert into address (city, street, zip_code) values (?,?,?)", "LD", "Homory Street", "5**3*0");
			return i + k;
		});
	}

}
