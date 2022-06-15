package view;

public class Item {
	public Item(String no, String value) {
		super();
		this.no = no;
		this.value = value;
	}

	String no;
	String value;

	@Override
	public boolean equals(Object obj) {
		return ((Item) obj).no.equals(no);
	}

	@Override
	public String toString() {
		return value;
	}
}
