package com.gcu.data;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.gcu.exception.DataServiceException;
import com.gcu.models.User;

public class UserDataService implements DataAccessInterface<User> {

	@SuppressWarnings("unused")
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplateObject;
	
	Logger logger = LoggerFactory.getLogger(UserDataService.class);

	/**
	 * @see DataAccessInterface
	 */
	@Override
	public List<User> viewAll() {
		logger.info("RecipeLogger---Class Entered: UserDataService.class, Method: viewAll()");
		logger.info("RecipeLogger---Data Layer: Grabbing a list of all users");
		// Creates a SQL statement to be filled in later
		String sql = "SELECT * FROM users INNER JOIN credentials ON credentials_ID = credentials.ID;";
		
		// Creates an ArrayList of users that will be filled with all the users from the
		// database
		List<User> userList = new ArrayList<User>();

		try {
			// Access the database and Queries for all users and is given a results set with
			// information of all users
			SqlRowSet srs = jdbcTemplateObject.queryForRowSet(sql);
			while (srs.next()) {
				userList.add(new User(srs.getInt("ID"), srs.getString("FIRSTNAME"), srs.getString("LASTNAME"),
						srs.getString("EMAIL"), srs.getString("PHONENUMBER"), srs.getString("USERNAME"),
						srs.getString("PASSWORD")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return userList;
	}

	/**
	 * @see DataAccessInterface
	 */
	@Override
	public User viewById(int id) {
		logger.info("RecipeLogger---Class Entered: UserDataService.class, Method: viewById()");
		logger.info("RecipeLogger---Data Layer: Grabbing a user from database with ID: " + id);
		// Creates a SQL statement to be filled in later
		String sql = "SELECT * FROM users INNER JOIN credentials ON credentials_ID = credentials.ID AND users.ID = ?;";

		// Creates an ArrayList of users that will be filled with all the users from the
		// database
		User currentUser = null;

		try {
			// Access the database and Queries for all users and is given a results set with
			// information of all users
			SqlRowSet srs = jdbcTemplateObject.queryForRowSet(sql, id);
			while (srs.next()) {
				currentUser = new User(srs.getInt("ID"), srs.getString("FIRSTNAME"), srs.getString("LASTNAME"),
						srs.getString("EMAIL"), srs.getString("PHONENUMBER"), srs.getString("USERNAME"),
						srs.getString("PASSWORD"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataServiceException(e);
		}

		return currentUser;
	}

	/**
	 * @see DataAccessInterface
	 */
	@Override
	public int create(User user, int id) {
		logger.info("RecipeLogger---Class Entered: UserDataService.class, Method: create()");
		logger.info("RecipeLogger---Data Layer: Adding user to database with Firstname: " + user.getFirstName());

		int returnNum = 0;

		// Checks if the user is valid
		String sqlValidUser = "SELECT * FROM credentials WHERE USERNAME=?;";

		try {

			SqlRowSet srsFind = jdbcTemplateObject.queryForRowSet(sqlValidUser, user.getCredentials().getUsername());

			if (srsFind.next() == false) {
				String sqlInsertCreds = "INSERT INTO credentials(ID, USERNAME, PASSWORD) VALUES(NULL, ?, ?)";
				String sqlInsertUser = "INSERT INTO users(ID, FIRSTNAME, LASTNAME, EMAIL, PHONENUMBER, credentials_ID) VALUES (NULL, ?, ?, ?, ?, ?)";

				try {
					// Inputs inforamtion into the database for both credentials and user
					// information
					int rows = jdbcTemplateObject.update(sqlInsertCreds, user.getCredentials().getUsername(),
							user.getCredentials().getPassword());

					String sqlQuery = "SELECT LAST_INSERT_ID() AS LAST_ID FROM credentials";

					SqlRowSet srs = jdbcTemplateObject.queryForRowSet(sqlQuery);
					srs.next();
					int userCredID = Integer.parseInt(srs.getString("LAST_ID"));

					rows += jdbcTemplateObject.update(sqlInsertUser, user.getFirstName(), user.getLastName(),
							user.getEmail(), user.getPhoneNumber(), userCredID);

					returnNum = rows;
				}

				catch (Exception e) {
					e.printStackTrace();
					throw new DataServiceException(e);
				}
			}

			else {
				returnNum = -1;
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			throw new DataServiceException(e);
		}

		return returnNum;
	}

	/**
	 * @see DataAccessInterface
	 */
	@Override
	public int update(User user, int id) {
		logger.info("RecipeLogger---Class Entered: UserDataService.class, Method: update()");
		logger.info("RecipeLogger---Data Layer: Updating user information with ID: " + id);
		int returnNum = 0;

		// Creates SQL statements to be filled in later
		String sqlQuery = "SELECT credentials_ID FROM users WHERE users.ID=?";
		String sqlUpdateCred = "UPDATE credentials SET USERNAME=?, PASSWORD=? WHERE ID=?";
		String sqlUpdateUser = "UPDATE users SET FIRSTNAME=?, LASTNAME=?, EMAIL=?, PHONENUMBER=? WHERE ID=?";

		try {
			// Updated information for both the crednetial and user information
			int userCredNum = 0;

			SqlRowSet srs = jdbcTemplateObject.queryForRowSet(sqlQuery, id);
			while (srs.next()) {
				userCredNum = srs.getInt("credentials_ID");
			}

			int rowsChanged = jdbcTemplateObject.update(sqlUpdateCred, user.getCredentials().getUsername(),
					user.getCredentials().getPassword(), userCredNum);

			rowsChanged += jdbcTemplateObject.update(sqlUpdateUser, user.getFirstName(), user.getLastName(),
					user.getEmail(), user.getPhoneNumber(), id);

			returnNum = rowsChanged;
		}

		catch (Exception e) {
			e.printStackTrace();
			throw new DataServiceException(e);
		}

		return returnNum;
	}

	/**
	 * @see DataAccessInterface
	 * @info LATER IMPLEMENTED
	 */
	@Override
	public int delete(int id) {
		// Use logic from delete() in recipedataservice
		return -1;
	}

	/**
	 * @see DataAccessInterface
	 * @info LATER IMPLEMENTED
	 */
	@Override
	public List<User> viewByParentId(int parentId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see DataAccessInterface
	 * @info LATER IMPLEMENTED
	 */
	@Override
	public User viewByObject(User t) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * setDataSouce takes in a DataSource from our web.xml in order to create a
	 * dataSource and JDBC Template Object used to connect and perform CRUD action
	 * to the database
	 * 
	 * @param ds - DataSource - to connect the sql command to the databses
	 */
	public void setDataSource(DataSource ds) {
		this.dataSource = ds;
		this.jdbcTemplateObject = new JdbcTemplate(ds);
	}


}
