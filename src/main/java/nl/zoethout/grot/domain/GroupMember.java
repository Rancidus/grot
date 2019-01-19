package nl.zoethout.grot.domain;

import java.util.Vector;
import java.util.Date;

public class GroupMember extends User {
	private String characterName;
	private int craft;
	public String transportation;
	public String transportationId;
	public String releaseInfo;
	public Date eventAvailable;
	public String eventStay;
	public String eventTransportation;
	
	public Vector myEvenement;
	public Group myGroep;
	
	public GroupMember() {
		super();
	}

	public String getCharacterName() {
		return characterName;
	}

	public void setCharacterName(String characterName) {
		this.characterName = characterName;
	}

	public int getCraft() {
		return craft;
	}

	public void setCraft(int craft) {
		this.craft = craft;
	}

	public String getTransportation() {
		return transportation;
	}

	public void setTransportation(String transportation) {
		this.transportation = transportation;
	}

	public String getTransportationId() {
		return transportationId;
	}

	public void setTransportationId(String transportationId) {
		this.transportationId = transportationId;
	}

	public String getReleaseInfo() {
		return releaseInfo;
	}

	public void setReleaseInfo(String releaseInfo) {
		this.releaseInfo = releaseInfo;
	}

	public Date getEventAvailable() {
		return eventAvailable;
	}

	public void setEventAvailable(Date eventAvailable) {
		this.eventAvailable = eventAvailable;
	}

	public String getEventStay() {
		return eventStay;
	}

	public void setEventStay(String eventStay) {
		this.eventStay = eventStay;
	}

	public String getEventTransportation() {
		return eventTransportation;
	}

	public void setEventTransportation(String eventTransportation) {
		this.eventTransportation = eventTransportation;
	}

}