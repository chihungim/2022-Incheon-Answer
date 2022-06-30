package view;

public class Item {
	private String key, value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Item(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		return getValue();
	}

	@Override
	public boolean equals(Object obj) {
		return ((Item) obj).getKey().equals(getKey());
	}
}
