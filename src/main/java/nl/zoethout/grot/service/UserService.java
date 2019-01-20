package nl.zoethout.grot.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nl.zoethout.grot.domain.Address;
import nl.zoethout.grot.domain.Role;
import nl.zoethout.grot.domain.User;

public interface UserService {
	public void saveUser(User user);

	public void saveAddress(Address address);

	public User readUser(int userId);

	public User readUser(String userName);

	public Address readAddress(int userId);

	/**
	 * Opvragen specifieke groep
	 */
	public Role readRole(String roleName);

	/**
	 * Opvragen alle groepen
	 */
	public List<Role> readRoles();

	public User loginUser(String userName, String password);

	public void setPrincipal(HttpServletRequest req, User usr);

	public List<User> listProfiles();
}