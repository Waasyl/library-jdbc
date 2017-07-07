package pl.sda.library.table.model;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import pl.sda.library.model.Book;

public class JdbcDataTableModel extends CrudDataTableModel {

	private static final long serialVersionUID = 1L;
	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/sda?useSSL=false";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "";

	public JdbcDataTableModel() {
		try {
			Class.forName(DB_DRIVER);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		filterByName("");
	}

	@Override
	public int getRowCount() {
		BigDecimal bigDecimal = null;
		try {
			Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("select count(*) from book");
			if(result.next()){
				bigDecimal = result.getBigDecimal(1);
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return bigDecimal.intValue();
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Book book = getByName(filter).get(rowIndex);
		switch (columnIndex) {
			case 0:
				return book.getId();
			case 1:
				return book.getTitle();
			case 2:
				return book.getAuthorFirstName();
			case 3:
				return book.getAuthorLastName();
			case 4:
				return book.getCategories();
			default:
				return null;
		}
	}

	@Override
	public Book getById(int id) {
		Book bookToReturn = new Book();
		Connection connection = null;
		try{
			connection = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD);
			PreparedStatement statement = connection.prepareStatement("select b.title as title, b.id as id, " +
					"a.first_name as firstName, a.last_name as lastName " +
					"from book as b join author as a on b.author_id = a.id where b.id = ?");
			statement.setInt(1, id);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				bookToReturn.setId(resultSet.getInt("id"));
				bookToReturn.setTitle(resultSet.getString("title"));
				bookToReturn.setAuthorFirstName(resultSet.getString("firstName"));
				bookToReturn.setAuthorLastName(resultSet.getString("lastName"));

			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return bookToReturn;
	}

	@Override
	public List<Book> getByName(String name) {
		List<Book> listOfBook = new LinkedList<>();
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			Statement statement = connection.createStatement();
			ResultSet executeQuery = statement.executeQuery("SELECT b.title AS title, b.id AS id," +
					" a.first_name AS firstName, a.last_name AS lastName" +
					" FROM book AS b JOIN author AS a ON b.author_id = a.id");
			//aby otrzymywac wyniki nalezy iterowac po "bazie" i gdy executeQuery.next ma nexta to bedziemy otrzymywac koleje tytuly
			while(executeQuery.next()) {
				Book book = new Book();
				book.setTitle(executeQuery.getString("title"));
				book.setId(executeQuery.getInt("id"));
				book.setAuthorFirstName(executeQuery.getString("firstName"));
				book.setAuthorLastName(executeQuery.getString("lastName"));
				listOfBook.add(book);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return listOfBook;
	}

	@Override
	public void create(Book book) {
		// TODO dodanie książki
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD);
			PreparedStatement statement = connection.prepareStatement("insert into author "
					+ "(first_name,last_name) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, book.getAuthorFirstName());
			statement.setString(2, book.getAuthorLastName());
			statement.executeUpdate();
			ResultSet result = statement.getGeneratedKeys();
			int authorId = 0;
			if(result.next()){
				authorId = result.getInt(1);
			}
			statement.close();
			statement = connection.prepareStatement("insert into book (title,author_id)"
					+ " VALUES (?,?)");
			statement.setString(1,book.getTitle());
			statement.setInt(2,authorId);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		refresh();
	}

	@Override
	public void update(Book book) {
		//TODO modyfikacja książki
		refresh();
	}

	@Override
	public void delete(Book book) {
		//TODO usunięcie książki
		refresh();
	}

}
