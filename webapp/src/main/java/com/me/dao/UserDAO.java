package com.me.dao;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import com.me.pojo.User;

@Repository
public class UserDAO extends DAO {

	public User get(String username) {
		User user = null;
		try {
			begin();
			Query<?> q = getSession().createQuery("from User where username= :username");
			q.setParameter("username", username);
			user = (User) q.uniqueResult();
			commit();
		} catch (HibernateException e) {
			rollback();
			e.printStackTrace();
		}
		return user;
	}

	public boolean add(User user) {
		try {
			begin();
			getSession().saveOrUpdate(user);
			;
			commit();
		} catch (HibernateException e) {
			rollback();
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean delete(User user) {
		try {
			begin();
			getSession().delete(user);
			commit();
		} catch (HibernateException e) {
			rollback();
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
