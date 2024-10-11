package me.despical.whackme.arena.blocks;

/**
 * @author Despical
 * <p>
 * Created at 11.10.2024
 */
public enum PointBlockType {

	GREEN_BLOCK("Punch-Me", "greenBlock"),

	RED_BLOCK("Dont-Punch-Me", "redBlock"),

	CYAN_BLOCK("Ouch", "cyanBlock");

	private final String tag;
	private final String path;

	PointBlockType(String path, String tag) {
		this.path = path;
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public String getPath() {
		return this.path;
	}
}