package nl.zoethout.grot.web;

import static nl.zoethout.grot.util.PageURL.POC;

import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nl.zoethout.grot.dao.UserDao;
import nl.zoethout.grot.domain.Address;
import nl.zoethout.grot.domain.Principal;
import nl.zoethout.grot.domain.Role;
import nl.zoethout.grot.domain.User;
import nl.zoethout.grot.domain.UserWrapper;
import nl.zoethout.grot.security.PseudoSecurity;
import nl.zoethout.grot.security.PseudoSecurityExecute;
import nl.zoethout.grot.service.UserService;
import nl.zoethout.grot.util.CountryCode;
import nl.zoethout.grot.util.TextUtil;
import nl.zoethout.grot.validation.AddressValidator;
import nl.zoethout.grot.validation.UserValidator;

@SuppressWarnings("unused")
@Controller // This class is a Controller
@RequestMapping(path = { "/poc", "/pocs" }) // URL's start with /demo (after Application path)
public class PocController extends WebController {
	private static final String DEFAULT_MESSAGE = "message";
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private ResourceBundle bundle = ResourceBundle.getBundle("poc");
	
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserService userService;

	@RequestMapping(value = "start", method = RequestMethod.GET)
	public String rmStart(Map<String, Object> model) {
		setAttributes();
		model.putAll(attributes);
		model.put(DEFAULT_MESSAGE, bundle.getString("MSG_START"));
		return POC.part();
	}

	private void setAttributes() {
		setText("DESCRIPTION");
		setText("VAL_START");
		setText("VAL_DAO");
		setText("VAL_PROPERTIES");
		setText("VAL_REPOSITORY");
		setText("VAL_SAVEUSER");
		setText("VAL_LOGIN_VALID");
		setText("VAL_LOGIN_INVALID");
	}

	private void setText(String key) {
		attributes.put(key, bundle.getString(key));
	}

	@RequestMapping(value = "dao", method = RequestMethod.GET)
	public String rmDao(Model model, HttpServletRequest req) {
		setAttributes();
		model.addAllAttributes(attributes);
		User user = userDao.readUser(1);
		Set<Role> roles = user.getRoles();
		model.addAttribute(DEFAULT_MESSAGE, bundle.getString("MSG_DAO") + roles);
		return POC.part();
	}

	// inject via application.properties
	@Value("${msg.welcome:test}")
	private String message = "Hello World";

	@RequestMapping(value = "properties", method = RequestMethod.GET)
	public String rmProperties(Map<String, Object> model) {
		setAttributes();
		model.putAll(attributes);
		model.put(DEFAULT_MESSAGE, bundle.getString("MSG_PROPERTIES") + this.message);
		return POC.part();
	}

	@RequestMapping(value = "/saveUser")
	public String rmSaveUser(Map<String, Object> model) {
		User user = userService.readUser("testu00");
		if (user == null) {
			user = testUser();
		}
		Address address = userService.readAddress(user.getUserId());
		if (address == null) {
			address = testAddress();
		}
		Member member = new Member(userService, user, address);
		member.save();
		return "redirect:/user/testu00";
	}

	private User testUser() {
		User user = new User();
		user.setUserName("testu00");
		user.setFirstName("Test");
		user.setLastName("User");
		user.setPrefix("");
		user.setSex("m");
		user.setPassword("123456");
		user.setEnabled(true);
		GregorianCalendar gregorianCalendar = new GregorianCalendar(2018, 0, 11);
		Date dateBirth = gregorianCalendar.getTime();
		user.setDateBirth(dateBirth);
		Date today = new Date(System.currentTimeMillis());
		user.setDateRegistered(today);
		return user;
	}

	private Address testAddress() {
		Address address = new Address();
		address.setStreetName("Test street");
		address.setStreetNumber("99");
		address.setZip("9999 XX");
		address.setCity("Test");
		address.setCountry("BE");
		address.setPhone1("1234567890");
		address.setPhone2("");
		address.setEmail1("testu00@domain.org");
		address.setEmail2("");
		return address;
	}

	@RequestMapping(value = "/loginValid")
	public String rmLoginValid(Map<String, Object> model) {
		setAttributes();
		model.putAll(attributes);
		User usr = userService.loginUser("Gerard", "123456");
		if (usr == null) {
			model.put(DEFAULT_MESSAGE, "Not found!");
		} else {
			model.put(DEFAULT_MESSAGE, usr.toString());
		}
		return POC.part();
	}

	@RequestMapping(value = "loginInvalid")
	public String rmLoginInvalid(Map<String, Object> model) {
		setAttributes();
		model.putAll(attributes);
		User usr = userService.loginUser("Thierry", "123456");
		if (usr == null) {
			model.put(DEFAULT_MESSAGE, "Not found!");
		} else {
			model.put(DEFAULT_MESSAGE, usr.toString());
		}
		return POC.part();
	}

	@RequestMapping(value = "denied")
	@PseudoSecurity(roles = { "admin", "user" })
	public String rmDenied(Map<String, Object> model, HttpServletRequest req, HttpServletResponse res) {
		// For testing : simulate a login
		String[] roles = { "huey", "duey", "luey" };
		HttpSession session = req.getSession();
		session.setAttribute("roles", roles);
		// Redirect on execution denied
		PseudoSecurityExecute.run(req, res, this.getClass());
		// Set navigational links
		setAttributes();
		// To page
		model.putAll(attributes);
		return POC.part();
	}

	@RequestMapping(value = "granted")
	@PseudoSecurity(roles = { "admin", "user" })
	public String rmGranted(Map<String, Object> model, HttpServletRequest req, HttpServletResponse res) {
		// For testing : simulate a login
		String[] roles = { "user" };
		HttpSession session = req.getSession();
		session.setAttribute("roles", roles);
		// Redirect on execution denied
		PseudoSecurityExecute.run(req, res, this.getClass());
		// Set navigational links
		setAttributes();
		// Useful code...
		model.put(DEFAULT_MESSAGE,
				"Access Granted" + "<br>Context : " + req.getContextPath() + "<br>QueryString : " + req.getQueryString()
						+ "<br>RequestURI : " + req.getRequestURI() + "<br>RequestURL : " + req.getRequestURL()
						+ "<br>ServletPath : " + req.getServletPath() + "<br>ServerName : " + req.getServerName()
						+ "<br>ServerPort : " + req.getServerPort() + "<br># ");
		// To page
		model.putAll(attributes);
		return POC.part();
	}

	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public String rmUserGet(Model model, HttpServletRequest req) {
		// Chosen member - username
		String username = "testu00";
		// Chosen member - reading
		User user = userService.readUser(username);
		// Chosen member - session
		provider(req).setSAFixed(user);
		// Chosen member - model (P19-02 / mutable user)
		model.addAttribute("mutable", user);
		// appropriate page
		return "user_writable";
	}

	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public String rmUserPost(Model model, @ModelAttribute("mutable") User user, BindingResult bindingResult,
			HttpServletRequest req) {
		// Make sure fields not on model do have value
		User fixedUser = provider(req).getSAFixed();
		user.setUserId(fixedUser.getUserId());
		user.setUserName(fixedUser.getUserName());
		user.setRoles(fixedUser.getRoles());
		Address address = user.getAddress();
		address.setUserId(fixedUser.getUserId());
		address.setUserName(fixedUser.getUserName());
		// Change authorisation
		editAuthorisation(userService, req, user);
		// Change case
		user.changeCase();
		address.changeCase();
		// instantiate validator
		AddressValidator addressValidator = new AddressValidator();
		UserValidator userValidator = new UserValidator(addressValidator);
		// validate
		userValidator.validate(user, bindingResult);
		// appropriate page
		if (bindingResult.hasErrors()) {
			String log = "";
			List<ObjectError> errors = bindingResult.getAllErrors();
			for (ObjectError error : errors) {
				if (error.getDefaultMessage() != null) {
					log += "<br><br><b>getCode</b> : " + error.getCode();
					log += "<br><b>getDefaultMessage</b> : " + error.getDefaultMessage();
					log += "<br><b>getObjectName</b> : " + error.getObjectName();
					log += "<br>" + error.toString();
				}
			}
			model.addAttribute("log", log);
			return "user_writable";
		} else {
			userService.saveUser(user);
			userService.saveAddress(address);
			// Page
			return "user_readable";
		}
	}

	@ModelAttribute("roles")
	public Map<String, String> maRoles(HttpServletRequest req) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		Principal principal = provider(req).getSAPrincipal();
		if (principal == null) {
			return null;
		} else if (principal.hasRole(ADM)) {
			for (Role role : userService.readRoles()) {
				result.put(role.getRoleName(), role.getRoleDesc());
			}
			return result;
		} else {
			return null;
		}
	}

	@ModelAttribute("countries")
	public CountryCode[] maCountries(HttpServletRequest req) {
		return CountryCode.values();
	}

	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);
		webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
		webDataBinder.registerCustomEditor(Role.class, new RoleEditor());
	}

	// https://www.logicbig.com/tutorials/spring-framework/spring-core/property-editors.html
	private class RoleEditor extends PropertyEditorSupport {
		@Override
		public void setAsText(String roleName) throws IllegalArgumentException {
			setValue(userService.readRole(roleName));
		}
	}

	private class Member extends UserWrapper {
		private UserService userService;
		private Address address;
		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		public Member() {
			super();
		}

		public Member(User user) {
			this.user = user;
			this.address = user.getAddress();
		}

		public Member(UserService userService, User user, Address address) {
			this.userService = userService;
			this.user = user;
			this.address = address;
		}

		public void revive(UserService service, int userId) {
			this.userService = service;
			setUser(userService.readUser(userId));
			setAddress(userService.readAddress(userId));
		}

		public void changeCase() {
			// UpperCase
			setZip(getZip().toUpperCase());
			// LowerCase
			setEmail1(getEmail1().toLowerCase());
			setEmail2(getEmail2().toLowerCase());
			// ProperCase
			setFirstName(TextUtil.toProperCase(getFirstName()));
			setLastName(TextUtil.toProperCase(getLastName()));
			setCity(TextUtil.toProperCase(getCity()));
			setStreetName(TextUtil.toProperCase(getStreetName()));
		}

		public boolean save() {
			boolean result = false;
			if (saveUser()) {
				if (saveAddress()) {
					user.setAddress(address);
					userService.saveUser(user);
					result = true;
				}
			}
			return result;
		}

		private boolean saveUser() {
			try {
				userService.saveUser(user);
				user = userService.readUser(user.getUserName());
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		private boolean saveAddress() {
			try {
				int id = user.getUserId();
				if (address.getUser() == null) {
					String name = user.getUserName();
					address.setUser(user);
					address.setUserId(id);
					address.setUserName(name);
				}
				userService.saveAddress(address);
				address = userService.readAddress(id);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		public UserService getUserService() {
			return userService;
		}

		public void setUserService(UserService userService) {
			this.userService = userService;
		}

		public Address getAddress() {
			return this.address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public List<String> getUserRoleNames() {
			List<String> result = new ArrayList<String>();
			Set<Role> roles = user.getRoles();
			for (Role role : roles) {
				result.add(role.getRoleDesc());
			}
			return result;
		}

		public String getDateBirthDisplay() {
			return sdf.format(user.getDateBirth());
		}

		public String getDateRegisteredDisplay() {
			return sdf.format(user.getDateRegistered());
		}

		public String getStreetName() {
			return address.getStreetName();
		}

		public void setStreetName(String streetName) {
			address.setStreetName(streetName);
		}

		public String getStreetNumber() {
			return address.getStreetNumber();
		}

		public void setStreetNumber(String streetNumber) {
			address.setStreetNumber(streetNumber);
		}

		public String getZip() {
			return address.getZip();
		}

		public void setZip(String zip) {
			address.setZip(zip);
		}

		public String getCity() {
			return address.getCity();
		}

		public void setCity(String city) {
			address.setCity(city);
		}

		public String getCountry() {
			return address.getCountry();
		}

		public String getCountryName() {
			return CountryCode.getName(address.getCountry());
		}

		public void setCountry(String country) {
			address.setCountry(country);
		}

		public String getPhone1() {
			return address.getPhone1();
		}

		public void setPhone1(String phone) {
			address.setPhone1(phone);
		}

		public String getPhone2() {
			return address.getPhone2();
		}

		public void setPhone2(String phone) {
			address.setPhone2(phone);
		}

		public String getEmail1() {
			return address.getEmail1();
		}

		public void setEmail1(String email) {
			address.setEmail1(email);
		}

		public String getEmail2() {
			return address.getEmail2();
		}

		public void setEmail2(String email) {
			address.setEmail2(email);
		}
	}
}
