package com.afforess.minecartmaniachestcontrol;

import java.io.File;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.afforess.minecartmaniacore.world.MinecartManiaWorld;
import com.afforess.minecartmaniacore.config.MinecartManiaConfigurationParser;
import com.afforess.minecartmaniacore.config.SettingParser;
import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;

public class ChestControlSettingParser implements SettingParser{
	private static final double version = 1.3;
	private static MinecartManiaLogger log = MinecartManiaLogger.getInstance();

	public boolean isUpToDate(Document document) {
		try {
			NodeList list = document.getElementsByTagName("version");
			Double version = MinecartManiaConfigurationParser.toDouble(list.item(0).getChildNodes().item(0).getNodeValue(), 0);
			log.debug("Chest Control Config read: version: " + list.item(0).getTextContent());
			if (version == 1.3) {
				//Place the code to update to the next version here
				//version = 1.4;	//This needs to be updated to the next version of the document.
				//list.item(0).setTextContent(version.toString());
			}
			return version == ChestControlSettingParser.version;
		}
		catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean read(Document document) {
		//Set the default configuration before we try to read anything.
		setDefaultConfiguration();

		NodeList list;
		try {
			list = document.getElementsByTagName("MinecartManiaConfiguration").item(0).getChildNodes();	//get the root nodes of the ConfigurationTree
			String elementChildName = "";		//holds the name of the node
			String elementChildValue = "";		//holds the value of the node
			//loop through each of the child nodes of the document
			for (int idx = 0; idx < list.getLength(); idx++) {
				Node elementChild = list.item(idx);	//extract the node
				elementChildName = "";				//reset the child name
				elementChildValue = null;			//reset the child value
				//do we have a valid element node
				if (elementChild.getNodeType() == Node.ELEMENT_NODE) {
					elementChildName = elementChild.getNodeName();	//get the node name
					elementChildValue = elementChild.getTextContent(); //get the node value
					if (elementChildValue != null && elementChildValue != "") {
						//Handle the possible nodes we have at this level.
						if (elementChildName == "version") {
							if (elementChildValue != String.valueOf(version)) { /* documentUpgrade(document); */ }
						} else if (elementChildName == "ChestDispenserSpawnDelay"
								|| elementChildName == "ItemCollectionRange"
								) {
							MinecartManiaWorld.getConfiguration().put(elementChildName, MinecartManiaConfigurationParser.toInt(elementChildValue, getDefaultConfigurationIntegerValue(elementChildName)));
							log.debug("Chest Control Config read: " + elementChildName + " = " + elementChildValue);
						} else if (elementChildName == "SpawnAtSpeed") {
							MinecartManiaWorld.getConfiguration().put(elementChildName, MinecartManiaConfigurationParser.toDouble(elementChildValue, getDefaultConfigurationDoubleValue(elementChildName)));
							log.debug("Chest Control Config read: " + elementChildName + " = " + elementChildValue);
						} else {
							log.info("Chest Control Config read unknown node: " + elementChildName);
						}
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		debugShowConfigs();
		return true;
	}
	private void setDefaultConfiguration() {
		//Create the default Configuration values
		MinecartManiaWorld.getConfiguration().put("SpawnAtSpeed",				getDefaultConfigurationDoubleValue("SpawnAtSpeed"));
		MinecartManiaWorld.getConfiguration().put("ChestDispenserSpawnDelay",	getDefaultConfigurationIntegerValue("ChestDispenserSpawnDelay"));
		MinecartManiaWorld.getConfiguration().put("ItemCollectionRange",		getDefaultConfigurationIntegerValue("ItemCollectionRange"));
	}
	private int getDefaultConfigurationIntegerValue(String ConfigName) {
		if (ConfigName == "ChestDispenserSpawnDelay") return (1000);
		if (ConfigName == "ItemCollectionRange") return (1);
		return 0;
	}
	private double getDefaultConfigurationDoubleValue(String ConfigName) {
		if (ConfigName == "SpawnAtSpeed") return (0.0);
		return 0;
	}

	private void debugShowConfigs() {
		//Display global configuration values
		for (Enumeration<String> ConfigKeys = MinecartManiaWorld.getConfiguration().keys(); ConfigKeys.hasMoreElements();) {
			String temp = ConfigKeys.nextElement();
			String value = MinecartManiaWorld.getConfigurationValue(temp).toString();
			if     (temp == "SpawnAtSpeed"
				 || temp == "ChestDispenserSpawnDelay"
				 || temp == "ItemCollectionRange"
				){
				log.debug("Chest Control Config: " + temp + " = " + value);
			}
		}
	}

	@Override
	public boolean write(File configuration, Document document) {
		try {
			if (document == null) {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				//root elements
				document = docBuilder.newDocument();
				document.setXmlStandalone(true);
				Element rootElement = document.createElement("MinecartManiaConfiguration");
				document.appendChild(rootElement);

				Element setting = document.createElement("version");
				setting.appendChild(document.createTextNode(String.valueOf(version)));
				rootElement.appendChild(setting);

				setting = document.createElement("SpawnAtSpeed");
				Comment comment = document.createComment("The speed that minecarts are spawned at. 0 by default. For reference, 0.6 is full speed (and the speed launchers launch at).");
				setting.appendChild(document.createTextNode("0.0"));
				rootElement.appendChild(setting);
				rootElement.insertBefore(comment,setting);

				setting = document.createElement("ChestDispenserSpawnDelay");
				comment = document.createComment("The delay (in milliseconds. 1000ms = 1s) between each minecart spawned at a chest dispenser.");
				setting.appendChild(document.createTextNode("1000"));
				rootElement.appendChild(setting);
				rootElement.insertBefore(comment,setting);

				setting = document.createElement("ItemCollectionRange");
				comment = document.createComment("The range (radius) in blocks a minecart will search for item collection, item depositing, or furnace signs");
				setting.appendChild(document.createTextNode("1"));
				rootElement.appendChild(setting);
				rootElement.insertBefore(comment,setting);
			}
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(configuration);
			transformer.transform(source, result);
		}
		catch (Exception e) { e.printStackTrace(); return false; }
		return true;
	}
}
