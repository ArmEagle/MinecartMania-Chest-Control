package com.afforess.minecartmaniachestcontrol;

import java.io.File;
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
import org.w3c.dom.NodeList;

import com.afforess.minecartmaniacore.MinecartManiaChest;
import com.afforess.minecartmaniacore.MinecartManiaWorld;
import com.afforess.minecartmaniacore.config.MinecartManiaConfigurationParser;
import com.afforess.minecartmaniacore.config.SettingParser;

public class ChestControlSettingParser implements SettingParser{
	private static final double version = 1.3;
	
	public boolean isUpToDate(Document document) {
		try {
			NodeList list = document.getElementsByTagName("version");
			Double version = MinecartManiaConfigurationParser.toDouble(list.item(0).getChildNodes().item(0).getNodeValue(), 0);
			return version == ChestControlSettingParser.version;
		}
		catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean read(Document document) {
		Object value;
		NodeList list;
		String setting;
		
		try {
			setting = "SpawnAtSpeed";
			list = document.getElementsByTagName(setting);
			value = MinecartManiaConfigurationParser.toDouble(list.item(0).getChildNodes().item(0).getNodeValue(), 0.0);
			MinecartManiaWorld.getConfiguration().put(setting, value);
			
			setting = "ChestDispenserSpawnDelay";
			list = document.getElementsByTagName(setting);
			value = MinecartManiaConfigurationParser.toInt(list.item(0).getChildNodes().item(0).getNodeValue(), 1000);
			MinecartManiaChest.SPAWN_DELAY = (Integer) value;
			
			setting = "ItemCollectionRange";
			list = document.getElementsByTagName(setting);
			value = MinecartManiaConfigurationParser.toInt(list.item(0).getChildNodes().item(0).getNodeValue(), 1000);
			MinecartManiaWorld.getConfiguration().put(setting, value);
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean write(File configuration) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			//root elements
			Document doc = docBuilder.newDocument();
			doc.setXmlStandalone(true);
			Element rootElement = doc.createElement("MinecartManiaConfiguration");
			doc.appendChild(rootElement);
			
			Element setting = doc.createElement("version");
			setting.appendChild(doc.createTextNode(String.valueOf(version)));
			rootElement.appendChild(setting);
			
			setting = doc.createElement("SpawnAtSpeed");
			Comment comment = doc.createComment("The speed that minecarts are spawned at. 0 by default. For reference, 0.6 is full speed (and the speed launchers launch at).");
			setting.appendChild(doc.createTextNode("0.0"));
			rootElement.appendChild(setting);
			rootElement.insertBefore(comment,setting);
			
			setting = doc.createElement("ChestDispenserSpawnDelay");
			comment = doc.createComment("The delay (in milliseconds. 1000ms = 1s) between each minecart spawned at a chest dispenser.");
			setting.appendChild(doc.createTextNode("1000"));
			rootElement.appendChild(setting);
			rootElement.insertBefore(comment,setting);
			
			setting = doc.createElement("ItemCollectionRange");
			comment = doc.createComment("The range (radius) in blocks a minecart will search for item collection, item depositing, or furnace signs");
			setting.appendChild(doc.createTextNode("1"));
			rootElement.appendChild(setting);
			rootElement.insertBefore(comment,setting);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(configuration);
			transformer.transform(source, result);
		}
		catch (Exception e) { e.printStackTrace(); return false; }
		return true;
	}
}
