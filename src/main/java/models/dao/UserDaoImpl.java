package main.java.models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import main.java.exceptions.InvalidUserException;
import main.java.exceptions.UserNotFoundException;
import main.java.exceptions.DaoException;
import main.java.managers.DBManager;
import main.java.models.User;
import main.java.models.idao.IUser;
import main.java.utils.SqlTable;

public class UserDaoImpl implements IUser<User> {

    SqlTable SQLTable = new SqlTable("users", new String[] {"id", "email", "password", "userType"});
    
    public UserDaoImpl() {
        
    }

    @Override
    public User get(Long id) throws DaoException {
        Connection connection = DBManager.getInstance().connect();

        try {
            PreparedStatement ps = null;
            ps = connection.prepareStatement( SQLTable.buildSelect("*", String.format("id = %d", id)) );
            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                return new User( rs.getLong("id"), rs.getString("email"), rs.getString("password"), rs.getString("userType") );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException("Error al recuperar el usuario.");
        }
        throw new UserNotFoundException("No se encontró al usuario.");
    }

    @Override
    public List<User> getAll() throws DaoException {
        List<User> users = new ArrayList<>();
        
		Connection connection = DBManager.getInstance().connect();
		try {
            PreparedStatement ps = null;
            ps = connection.prepareStatement( SQLTable.buildSelect("*") );
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
                User user = new User( rs.getLong("id"), rs.getString("email"), rs.getString("password"), rs.getString("userType") );
                users.add(user);
            }
		} catch (SQLException e) {
			try {
                connection.rollback();
                e.printStackTrace();
			} catch (SQLException e1) {
                e1.printStackTrace();
            }
            throw new DaoException("Error al recuperar los usuarios");
		} finally {
			try {
				connection.close();
			} catch (SQLException e1) {
                e1.printStackTrace();
                throw new DaoException("Error al terminar la conexión");
             }
        }
        return users; // return users
    }

    // Should I throw exception in those methods too?
    @Override
    public User insert(User t) throws DaoException {
		Connection connection = DBManager.getInstance().connect();
		try {
            PreparedStatement ps = connection.prepareStatement( SQLTable.buildInsert() );
            ps.setString( SQLTable.getFieldIndex("email"), t.getEmail() );
            ps.setString( SQLTable.getFieldIndex("password"), t.getPassword() );
            ps.setString( SQLTable.getFieldIndex("userType"), t.getUserType() );
			int affectedRows = ps.executeUpdate();
            connection.commit();

            if (affectedRows == 0) {
                throw new DaoException("Error al crear el usuario.");
            }

            PreparedStatement s = connection.prepareStatement("CALL IDENTITY()");
            ResultSet rs = s.executeQuery();
            if(rs.next()) {
                t.setId( rs.getLong( SQLTable.getFieldIndex("id") ) );
            }
            else {
                throw new DaoException("Error al crear el usuario, no se pudo obtener el ID.");
            }
		} catch (SQLException e) {
			try {
                connection.rollback();
                e.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				connection.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
        return t;
    }
    
    @Override
    public int update(User t) throws DaoException {

        if(t.getId() == null) {
            throw new InvalidUserException("El ID de usuario es invalido.");
        }

        Connection connection = DBManager.getInstance().connect();
        int affectedRows = 0;
        try {

            PreparedStatement ps = null;
            ps = connection.prepareStatement( String.format("%s WHERE id = %d", SQLTable.buildUpdate(), t.getId()) );
            ps.setLong( SQLTable.getFieldIndex("id"), t.getId());
            ps.setString( SQLTable.getFieldIndex("email"), t.getEmail());
            ps.setString( SQLTable.getFieldIndex("password"), t.getPassword());
			affectedRows = ps.executeUpdate();
            connection.commit();
            if (affectedRows == 0) {
                throw new DaoException("Error al actualizar el usuario.");
            }
        } catch (SQLException e) {
			try {
				connection.rollback();
				e.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				connection.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
        return affectedRows;
    }

    @Override
    public int delete(User t) throws DaoException {

        if(t.getId() == null) {
            throw new InvalidUserException("El ID de usuario es invalido.");
        }

        Connection connection = DBManager.getInstance().connect();
        int affectedRows = 0;
		try {
            PreparedStatement ps = connection.prepareStatement(String.format("DELETE FROM %s WHERE id = %d", SQLTable.getTableName(), t.getId()));
			affectedRows = ps.executeUpdate();
            connection.commit();
		} catch (SQLException e) {
			try {
				connection.rollback();
				e.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				connection.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
        return affectedRows;
    }
     
 
}