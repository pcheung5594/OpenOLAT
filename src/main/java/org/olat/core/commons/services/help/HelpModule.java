/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.admin.help.ui.HelpAdminController;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.help.spi.AcademyLinkSPI;
import org.olat.core.commons.services.help.spi.ConfluenceLinkSPI;
import org.olat.core.commons.services.help.spi.CourseHelpSPI;
import org.olat.core.commons.services.help.spi.CustomLink1SPI;
import org.olat.core.commons.services.help.spi.CustomLink2SPI;
import org.olat.core.commons.services.help.spi.CustomLink3SPI;
import org.olat.core.commons.services.help.spi.SupportMailSPI;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 07.01.2015<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class HelpModule extends AbstractSpringModule {

	private final static String DELIMITER = ",";

	public final static String ACADEMY = "academy";
	public final static String ACADEMY_KEY = "ooAcademyLinkHelp";
	public final static String CONFLUENCE = "confluence";
	public final static String CONFLUENCE_KEY = "ooConfluenceLinkHelp";
	public final static String SUPPORT = "support";
	public final static String SUPPORT_KEY = "supportMailHelp";
	public final static String COURSE = "course";
	public final static String COURSE_KEY = "courseHelp";
	public final static String CUSTOM_1 = "custom1";
	public final static String CUSTOM_1_KEY = "customLink1Help";
	public final static String CUSTOM_2 = "custom2";
	public final static String CUSTOM_2_KEY = "customLink2Help";
	public final static String CUSTOM_3 = "custom3";
	public final static String CUSTOM_3_KEY = "customLink3Help";

	public final static String AUTHORSITE = "authorsite";
	public final static String USERTOOL = "usertool";
	public final static String DMZ = "dmz";

	public final static String DEFAULT_ICON = "o_icon_help";

	private List<String> helpPluginList;
	private List<HelpLinkSPI> userToolHelpPlugins;
	private List<HelpLinkSPI> authorSiteHelpPlugins;
	private List<HelpLinkSPI> dmzHelpPlugins;

	// General help settings
	@Value("${help.enabled:true}")
	private boolean helpEnabled;
	@Value("${help.plugin:ooConfluenceLinkHelp}")
	private String helpPlugins;

	// Academy settings
	@Value("${help.academy.link:https://www.openolat.org/academy}")
	private String academyLink;
	@Value("${help.academy.enabled:usertool,authorsite}")
	private String academyEnabled;
	@Value("${help.academy.icon:o_icon_video}")
	private String academyIcon;
	private int academyPos;

	// Confluence settings
	@Value("${help.confluence.enabled:usertool,authorsite}")
	private String confluenceEnabled;
	@Value("${help.confluence.icon:o_icon_manual}")
	private String confluenceIcon;
	private int confluencePos;

	// Support settings
	@Value("${help.support.email:mail@your.domain}")
	private String supportEmail;
	@Value("${help.support.enabled:}")
	private String supportEnabled;
	private String supportIcon;
	private int supportPos;

	// Course settings
	@Value("${help.course.softkey:OLAT::help-course_de.zip}")
	private String courseSoftkey;
	@Value("${help.course.enabled:}")
	private String courseEnabled;
	private String courseIcon;
	private int coursePos;

	// Custom link 1 settings
	@Value("${help.custom1.link:}")
	private String custom1Link;
	@Value("${help.custom1.new.window:false}")
	private boolean custom1NewWindow;
	@Value("${help.custom1.enabled:}")
	private String custom1Enabled;
	private String custom1Icon;
	private int custom1Pos;

	// Custom link 2 settings
	@Value("${help.custom2.link:}")
	private String custom2Link;
	@Value("${help.custom2.new.window:false}")
	private boolean custom2NewWindow;
	@Value("${help.custom2.enabled:}")
	private String custom2Enabled;
	private String custom2Icon;
	private int custom2Pos;

	// Custom link 3 settings
	@Value("${help.custom3.link:}")
	private String custom3Link;
	@Value("${help.custom3.new.window:false}")
	private boolean custom3NewWindow;
	@Value("${help.custom3.enabled:}")
	private String custom3Enabled;
	private String custom3Icon;
	private int custom3Pos;
	
	@Autowired
	I18nManager i18nManager;
	@Autowired
	I18nModule i18nModule;

	public HelpModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		loadProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		loadProperties();
	}

	private void loadProperties() {
		// General help settings
		helpPlugins = getStringPropertyValue("help.plugin", helpPlugins);

		// Academy settings
		academyLink = getStringPropertyValue("help.academy.link", academyLink);
		academyEnabled = getStringPropertyValue("help.academy.enabled", academyEnabled);
		academyIcon = getStringPropertyValue("help.academy.icon", academyIcon);
		academyPos = getIntPropertyValue("help.academy.pos");

		// Confluence settings
		confluenceEnabled = getStringPropertyValue("help.confluence.enabled", confluenceEnabled);
		confluenceIcon = getStringPropertyValue("help.confluence.icon", confluenceIcon);
		confluencePos = getIntPropertyValue("help.confluence.pos");

		// Support settings
		supportEmail = getStringPropertyValue("help.support.email", supportEmail);
		supportEnabled = getStringPropertyValue("help.support.enabled", supportEnabled);
		supportIcon = getStringPropertyValue("help.support.icon", DEFAULT_ICON);
		supportPos = getIntPropertyValue("help.support.pos");

		// Course settings
		courseSoftkey = getStringPropertyValue("help.course.softkey", courseSoftkey);
		courseEnabled = getStringPropertyValue("help.course.enabled", courseEnabled);
		courseIcon = getStringPropertyValue("help.course.icon", DEFAULT_ICON);
		coursePos = getIntPropertyValue("help.course.pos");

		// Custom link 1 settings
		custom1Link = getStringPropertyValue("help.custom1.link", custom1Link);
		custom1Enabled = getStringPropertyValue("help.custom1.enabled", custom1Enabled);
		custom1NewWindow = getBooleanPropertyValue("help.custom1.new.window");
		custom1Icon = getStringPropertyValue("help.custom1.icon", DEFAULT_ICON);

		// Custom link 2 settings
		custom2Link = getStringPropertyValue("help.custom2.link", custom2Link);
		custom2Enabled = getStringPropertyValue("help.custom2.enabled", custom2Enabled);
		custom2NewWindow = getBooleanPropertyValue("help.custom2.new.window");
		custom2Icon = getStringPropertyValue("help.custom2.icon", DEFAULT_ICON);

		// Custom link 2 settings
		custom3Link = getStringPropertyValue("help.custom3.link", custom3Link);
		custom3Enabled = getStringPropertyValue("help.custom3.enabled", custom3Enabled);
		custom3NewWindow = getBooleanPropertyValue("help.custom3.new.window");
		custom3Icon = getStringPropertyValue("help.custom3.icon", DEFAULT_ICON);
	}

	// CRUD operations
	public HelpLinkSPI getManualProvider() {
		// Manual provider for tool tips
		return (ConfluenceLinkSPI)CoreSpringFactory.getBean("ooConfluenceLinkHelp");
	}

	public void deleteHelpPlugin(String plugin) {
		// Position of the plugin which should be deleted
		// Value is set to 100 because a greater or equals filter is applied later
		int removalPosition = 100;
		
		// Remove language overlays
		Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
		for (String locale : i18nModule.getEnabledLanguageKeys()) {
			I18nItem item = i18nManager.getI18nItem(HelpAdminController.class.getPackage().getName(), "help." + plugin, allOverlays.get(Locale.forLanguageTag(locale)));
			i18nManager.saveOrUpdateI18nItem(item, "");
		}
		
		// Remove settings
		switch (plugin) {
		case ACADEMY:
			plugin = ACADEMY_KEY;
			removalPosition = academyPos;
			removeProperty("help.academy.link", true);
			removeProperty("help.academy.icon", true);
			removeProperty("help.academy.enabled", true);
			removeProperty("help.academy.pos", true);
			break;
		case CONFLUENCE:
			plugin = CONFLUENCE_KEY;
			removalPosition = confluencePos;
			removeProperty("help.confluence.icon", true);
			removeProperty("help.confluence.enabled", true);
			removeProperty("help.confluence.pos", true);
			break;
		case COURSE:
			plugin = COURSE_KEY;
			removalPosition = coursePos;
			removeProperty("help.course.softkey", true);
			removeProperty("help.course.icon", true);
			removeProperty("help.course.enabled", true);
			removeProperty("help.course.pos", true);
			break;
		case CUSTOM_1:
			plugin = CUSTOM_1_KEY;
			removalPosition = custom1Pos;
			removeProperty("help.custom1.link", true);
			removeProperty("help.custom1.icon", true);
			removeProperty("help.custom1.enabled", true);
			removeProperty("help.custom1.new.window", true);
			removeProperty("help.custom1.pos", true);
			break;
		case CUSTOM_2:
			plugin = CUSTOM_2_KEY;
			removalPosition = custom2Pos;
			removeProperty("help.custom2.link", true);
			removeProperty("help.custom2.icon", true);
			removeProperty("help.custom2.enabled", true);
			removeProperty("help.custom2.new.window", true);
			removeProperty("help.custom2.pos", true);
			break;
		case CUSTOM_3:
			plugin = CUSTOM_3_KEY;
			removalPosition = custom3Pos;
			removeProperty("help.custom3.link", true);
			removeProperty("help.custom3.icon", true);
			removeProperty("help.custom3.enabled", true);
			removeProperty("help.custom3.new.window", true);
			removeProperty("help.custom3.pos", true);
			break;
		case SUPPORT:
			plugin = SUPPORT_KEY;
			removalPosition = supportPos;
			removeProperty("help.support.email", true);
			removeProperty("help.support.icon", true);
			removeProperty("help.support.enabled", true);
			removeProperty("help.support.pos", true);
			break;
		default:
			break;
		}
		
		// Remove help plugin
		helpPluginList.remove(plugin);
		helpPlugins = helpPluginList.stream().collect(Collectors.joining(DELIMITER));
		setStringProperty("help.plugin", helpPlugins, true);
		
		// Adapt positions
		for (String helpPlugin : helpPluginList) {
			switch (helpPlugin) {
			case ACADEMY_KEY:
				if (academyPos > removalPosition) {
					academyPos -= 1;
					setIntProperty("help.academy.pos", academyPos, true);
				}
				break;
			case CONFLUENCE_KEY:
				if (confluencePos > removalPosition) {
					confluencePos -= 1;
					setIntProperty("help.confluence.pos", confluencePos, true);
				}
				break;
			case COURSE_KEY:
				if (coursePos > removalPosition) {
					coursePos -= 1;
					setIntProperty("help.course.pos", coursePos, true);
				}
				break;
			case CUSTOM_1_KEY:
				if (custom1Pos > removalPosition) {
					custom1Pos -= 1;
					setIntProperty("help.custom1.pos", custom1Pos, true);
				}
				break;
			case CUSTOM_2_KEY:
				if (custom2Pos > removalPosition) {
					custom2Pos -= 1;
					setIntProperty("help.custom2.pos", custom2Pos, true);
				}
				break;
			case CUSTOM_3_KEY:
				if (custom3Pos > removalPosition) {
					custom3Pos -= 1;
					setIntProperty("help.custom3.pos", custom3Pos, true);
				}
				break;
			case SUPPORT_KEY:
				if (supportPos > removalPosition) {
					supportPos -= 1;
					setIntProperty("help.support.pos", supportPos, true);
				}
				break;
			default:
				break;
			}
		}
		
		loadLists();
	}

	public void saveHelpPlugin(String plugin, String icon, String input,
			boolean usertool, boolean authorsite, boolean login, boolean newWindow) {
		switch (plugin) {
		case ACADEMY:
			academyLink = setStringProperty("help.academy.link", input, true);
			academyIcon = setStringProperty("help.academy.icon", icon, true);
			academyEnabled = setStringProperty("help.academy.enabled", generateEnabledString(usertool, authorsite, login), true);
			addToHelpPlugins(ACADEMY_KEY);
			break;
		case CONFLUENCE:
			confluenceEnabled = setStringProperty("help.confluence.enabled", generateEnabledString(usertool, authorsite, login), true);
			confluenceIcon = setStringProperty("help.confluence.icon", icon, true);
			addToHelpPlugins(CONFLUENCE_KEY);
			break;
		case COURSE:
			courseSoftkey = setStringProperty("help.course.softkey", input, true);
			courseEnabled = setStringProperty("help.course.enabled", generateEnabledString(usertool, authorsite, login), true);
			courseIcon = setStringProperty("help.course.icon", icon, true);
			addToHelpPlugins(COURSE_KEY);
			break;
		case CUSTOM_1:
			custom1Link = setStringProperty("help.custom1.link", input, true);
			custom1NewWindow = newWindow; 
			setBooleanProperty("help.custom1.new.window", newWindow, true);
			custom1Enabled = setStringProperty("help.custom1.enabled", generateEnabledString(usertool, authorsite, login), true);
			custom1Icon = setStringProperty("help.custom1.icon", icon, true);
			addToHelpPlugins(CUSTOM_1_KEY);
			break;
		case CUSTOM_2:
			custom2Link = setStringProperty("help.custom2.link", input, true);
			custom2NewWindow = newWindow;
			setBooleanProperty("help.custom2.new.window", newWindow, true);
			custom2Enabled = setStringProperty("help.custom2.enabled", generateEnabledString(usertool, authorsite, login), true);
			custom2Icon = setStringProperty("help.custom2.icon", icon, true);
			addToHelpPlugins(CUSTOM_2_KEY);
			break;
		case CUSTOM_3:
			custom3Link = setStringProperty("help.custom3.link", input, true);
			custom3NewWindow = newWindow;
			setBooleanProperty("help.custom3.new.window", newWindow, true);
			custom3Enabled = setStringProperty("help.custom3.enabled", generateEnabledString(usertool, authorsite, login), true);
			custom3Icon = setStringProperty("help.custom3.icon", icon, true);
			addToHelpPlugins(CUSTOM_3_KEY);
			break;
		case SUPPORT:
			supportEmail = setStringProperty("help.support.email", input, true);
			supportEnabled = setStringProperty("help.support.enabled", generateEnabledString(usertool, authorsite, login), true);
			supportIcon = setStringProperty("help.support.icon", icon, true);
			addToHelpPlugins(SUPPORT_KEY);
			break;
		default:
			break;
		}

		loadLists();
	}

	// Getters and setters
	public boolean isHelpEnabled() {
		return helpEnabled;
	}
	
	/**
	 * Returns whether the confluence manual is enabled or not
	 * @return boolean
	 */
	public boolean isManualEnabled() {
		return isHelpEnabled() && helpPluginList.contains(CONFLUENCE_KEY);
	}

	public void setHelpEnabled(boolean helpEnabled) {
		this.helpEnabled = helpEnabled;
		setBooleanProperty("help.enabled", helpEnabled, true);
	}
	
	// Saves the order position
	public void setPosition(String helpPlugin, int position) {
		switch (helpPlugin) {
		case ACADEMY:
			academyPos = position;
			setIntProperty("help.academy.pos", position, true);
			break;
		case CONFLUENCE:
			confluencePos = position;
			setIntProperty("help.confluence.pos", position, true);
			break;
		case COURSE:
			coursePos = position;
			setIntProperty("help.course.pos", position, true);
			break;
		case CUSTOM_1:
			custom1Pos = position;
			setIntProperty("help.custom1.pos", position, true);
			break;
		case CUSTOM_2:
			custom2Pos = position;
			setIntProperty("help.custom2.pos", position, true);
			break;
		case CUSTOM_3:
			custom3Pos = position;
			setIntProperty("help.custom3.pos", position, true);
			break;
		case SUPPORT:
			supportPos = position;
			setIntProperty("help.support.pos", position, true);
			break;
		default:
			break;
		}
		
		loadLists();
	}

	public List<String> getHelpPluginList() {
		if (helpPluginList == null) {
			loadLists();
		}
		
		return helpPluginList;
	}

	public List<HelpLinkSPI> getDMZHelpPlugins() {
		if (dmzHelpPlugins == null) {
			loadLists();
		}

		return dmzHelpPlugins;
	}
	public List<HelpLinkSPI> getAuthorSiteHelpPlugins() {
		if (authorSiteHelpPlugins == null) {
			loadLists();
		}

		return authorSiteHelpPlugins;
	}
	public List<HelpLinkSPI> getUserToolHelpPlugins() {
		if (userToolHelpPlugins == null) {
			loadLists();
		}

		return userToolHelpPlugins;
	}

	public String getAcademyLink() {
		return academyLink;
	}

	public String getAcademyEnabled() {
		return academyEnabled;
	}

	public String getAcademyIcon() {
		return academyIcon;
	}

	public String getConfluenceEnabled() {
		return confluenceEnabled;
	}

	public String getConfluenceIcon() {
		return confluenceIcon;
	}

	public String getSupportEmail() {
		return supportEmail;
	}

	public String getSupportEnabled() {
		return supportEnabled;
	}

	public String getSupportIcon() {
		return supportIcon;
	}

	public String getCourseSoftkey() {
		return courseSoftkey;
	}

	public String getCourseEnabled() {
		return courseEnabled;
	}

	public String getCourseIcon() {
		return courseIcon;
	}

	public String getCustom1Link() {
		return custom1Link;
	}

	public boolean isCustom1NewWindow() {
		return custom1NewWindow;
	}

	public String getCustom1Enabled() {
		return custom1Enabled;
	}

	public String getCustom1Icon() {
		return custom1Icon;
	}

	public String getCustom2Link() {
		return custom2Link;
	}

	public boolean isCustom2NewWindow() {
		return custom2NewWindow;
	}

	public String getCustom2Enabled() {
		return custom2Enabled;
	}

	public String getCustom2Icon() {
		return custom2Icon;
	}

	public String getCustom3Link() {
		return custom3Link;
	}

	public boolean isCustom3NewWindow() {
		return custom3NewWindow;
	}

	public String getCustom3Enabled() {
		return custom3Enabled;
	}

	public String getCustom3Icon() {
		return custom3Icon;
	}

	/**
	 * Returns the not yet configured help plugins
	 * @return String[]
	 */
	public String[] getRemainingPlugins() {
		List<String> remainingPlugins = new ArrayList<>();

		if (!helpPluginList.contains(HelpModule.ACADEMY_KEY)) {
			remainingPlugins.add(ACADEMY);
		} if (!helpPluginList.contains(HelpModule.CONFLUENCE_KEY)) {
			remainingPlugins.add(CONFLUENCE);
		} if (!helpPluginList.contains(HelpModule.SUPPORT_KEY)) {
			remainingPlugins.add(SUPPORT);
		} if (!helpPluginList.contains(HelpModule.COURSE_KEY)) {
			remainingPlugins.add(COURSE);
		} if (!helpPluginList.contains(HelpModule.CUSTOM_1_KEY)) {
			remainingPlugins.add(CUSTOM_1);
		} if (!helpPluginList.contains(HelpModule.CUSTOM_2_KEY)) {
			remainingPlugins.add(CUSTOM_2);
		} if (!helpPluginList.contains(HelpModule.CUSTOM_3_KEY)) {
			remainingPlugins.add(CUSTOM_3);
		}

		return remainingPlugins.toArray(new String[remainingPlugins.size()]);
	}

	// Helpers	
	/**
	 * Reloads all lists containing the help plugins for different locations
	 */
	private void loadLists() {
		helpPluginList = new ArrayList<>(Arrays.asList(helpPlugins.split(DELIMITER)));
		helpPluginList.removeAll(Arrays.asList("",null));
		
		userToolHelpPlugins = new ArrayList<>();
		authorSiteHelpPlugins = new ArrayList<>();
		dmzHelpPlugins = new ArrayList<>();
		
		for (String helpPlugin : Arrays.asList(helpPlugins.split(DELIMITER))) {
			switch (helpPlugin) {
			case ACADEMY_KEY:
				if (helpPluginList.indexOf(helpPlugin) != academyPos) {
					Collections.swap(helpPluginList, academyPos, helpPluginList.indexOf(helpPlugin));
				}
				break;
			case CONFLUENCE_KEY:
				if (helpPluginList.indexOf(helpPlugin) != confluencePos) {
					Collections.swap(helpPluginList, confluencePos, helpPluginList.indexOf(helpPlugin));
				}
				break;
			case COURSE_KEY:
				if (helpPluginList.indexOf(helpPlugin) != coursePos) {
					Collections.swap(helpPluginList, coursePos, helpPluginList.indexOf(helpPlugin));
				}
				break;
			case SUPPORT_KEY:
				if (helpPluginList.indexOf(helpPlugin) != supportPos) {
					Collections.swap(helpPluginList, supportPos, helpPluginList.indexOf(helpPlugin));
				}
				break;
			case CUSTOM_1_KEY:
				if (helpPluginList.indexOf(helpPlugin) != custom1Pos) {
					Collections.swap(helpPluginList, custom1Pos, helpPluginList.indexOf(helpPlugin));
				}
				break;
			case CUSTOM_2_KEY:
				if (helpPluginList.indexOf(helpPlugin) != custom2Pos) {
					Collections.swap(helpPluginList, custom2Pos, helpPluginList.indexOf(helpPlugin));
				}
				break;
			case CUSTOM_3_KEY:
				if (helpPluginList.indexOf(helpPlugin) != custom3Pos) {
					Collections.swap(helpPluginList, custom3Pos, helpPluginList.indexOf(helpPlugin));
				}
				break;

			default:
				break;
			}
		}

		for (String helpPlugin : helpPluginList) {
			switch (helpPlugin) {
			case ACADEMY_KEY:
				AcademyLinkSPI academyHelpLinkSPI = (AcademyLinkSPI) CoreSpringFactory.getBean(helpPlugin);
				if (academyEnabled.contains(USERTOOL)) {
					userToolHelpPlugins.add(academyHelpLinkSPI);
				} if (academyEnabled.contains(AUTHORSITE)) {
					authorSiteHelpPlugins.add(academyHelpLinkSPI);
				} if (academyEnabled.contains(DMZ)) {
					dmzHelpPlugins.add(academyHelpLinkSPI);
				}
				break;
			case CONFLUENCE_KEY:
				ConfluenceLinkSPI confluenceHelpLinkSPI = (ConfluenceLinkSPI) CoreSpringFactory.getBean(helpPlugin);
				if (confluenceEnabled.contains(USERTOOL)) {
					userToolHelpPlugins.add(confluenceHelpLinkSPI);
				} if (confluenceEnabled.contains(AUTHORSITE)) {
					authorSiteHelpPlugins.add(confluenceHelpLinkSPI);
				} if (confluenceEnabled.contains(DMZ)) {
					dmzHelpPlugins.add(confluenceHelpLinkSPI);
				}
				break;
			case COURSE_KEY:
				CourseHelpSPI courseHelpLinkSPI = (CourseHelpSPI) CoreSpringFactory.getBean(helpPlugin);
				if (courseEnabled.contains(USERTOOL)) {
					userToolHelpPlugins.add(courseHelpLinkSPI);
				} if (courseEnabled.contains(AUTHORSITE)) {
					authorSiteHelpPlugins.add(courseHelpLinkSPI);
				} if (courseEnabled.contains(DMZ)) {
					dmzHelpPlugins.add(courseHelpLinkSPI);
				}
				break;
			case SUPPORT_KEY:
				SupportMailSPI supportMailHelpSPI = (SupportMailSPI) CoreSpringFactory.getBean(helpPlugin);
				if (supportEnabled.contains(USERTOOL)) {
					userToolHelpPlugins.add(supportMailHelpSPI);
				} if (supportEnabled.contains(AUTHORSITE)) {
					authorSiteHelpPlugins.add(supportMailHelpSPI);
				} if (supportEnabled.contains(DMZ)) {
					dmzHelpPlugins.add(supportMailHelpSPI);
				}
				break;
			case CUSTOM_1_KEY:
				CustomLink1SPI customLink1SPI = (CustomLink1SPI) CoreSpringFactory.getBean(helpPlugin);
				if (custom1Enabled.contains(USERTOOL)) {
					userToolHelpPlugins.add(customLink1SPI);
				} if (custom1Enabled.contains(AUTHORSITE)) {
					authorSiteHelpPlugins.add(customLink1SPI);
				} if (custom1Enabled.contains(DMZ)) {
					dmzHelpPlugins.add(customLink1SPI);
				}
				break;
			case CUSTOM_2_KEY:
				CustomLink2SPI customLink2SPI = (CustomLink2SPI) CoreSpringFactory.getBean(helpPlugin);
				if (custom2Enabled.contains(USERTOOL)) {
					userToolHelpPlugins.add(customLink2SPI);
				} if (custom2Enabled.contains(AUTHORSITE)) {
					authorSiteHelpPlugins.add(customLink2SPI);
				} if (custom2Enabled.contains(DMZ)) {
					dmzHelpPlugins.add(customLink2SPI);
				}
				break;
			case CUSTOM_3_KEY:
				CustomLink3SPI customLink3SPI = (CustomLink3SPI) CoreSpringFactory.getBean(helpPlugin);
				if (custom3Enabled.contains(USERTOOL)) {
					userToolHelpPlugins.add(customLink3SPI);
				} if (custom3Enabled.contains(AUTHORSITE)) {
					authorSiteHelpPlugins.add(customLink3SPI);
				} if (custom3Enabled.contains(DMZ)) {
					dmzHelpPlugins.add(customLink3SPI);
				}
				break;

			default:
				break;
			}
		}
	}
	
	/**
	 * Returns a string with locations where the help plugin should be displayed
	 * 
	 * @param usertool
	 * @param authorsite
	 * @param login
	 * @return
	 */
	private String generateEnabledString(boolean usertool, boolean authorsite, boolean login) {
		String enabled = usertool ? USERTOOL : "";
		enabled += authorsite ? "," + AUTHORSITE : "";
		enabled += login ? "," + DMZ : "";

		return enabled;
	}

	/**
	 * Adds a help plugin to the configuration 
	 * 
	 * @param plugin
	 */
	private void addToHelpPlugins(String plugin) {
		if (!helpPluginList.contains(plugin)) {
			helpPluginList.add(plugin);
		}

		helpPlugins = helpPluginList.stream().collect(Collectors.joining(DELIMITER));
		setStringProperty("help.plugin", helpPlugins, true);
	}
}