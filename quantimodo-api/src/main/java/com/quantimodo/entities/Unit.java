package com.quantimodo.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "`qm-units`")
public final class Unit {
	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "`abbreviated-name`")
	private String abbreviatedName;

	@ManyToOne
	@JoinColumn(name = "category")
	private UnitCategory category;

	@OneToMany(mappedBy = "unit")
	private List<UnitConversion> unitConversions;

	public String getName() {
		return name;
	}

	public Unit(String name, String abbreviatedName, UnitCategory category) {
		this.name = name;
		this.abbreviatedName = abbreviatedName;
		this.category = category;
	}

	public Long getId() {
		return id;
	}

	public String getAbbreviatedName() {
		return abbreviatedName;
	}

	public UnitCategory getCategory() {
		return category;
	}

	public List<UnitConversion> getUnitConversions() {
		return unitConversions;
	}
}
