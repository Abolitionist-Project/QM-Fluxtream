package com.fluxtream.connectors.myfitnesspal;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.search.annotations.Indexed;

import com.fluxtream.connectors.annotations.ObjectTypeSpec;
import com.fluxtream.domain.AbstractFacet;

/**
 * @author alucab
 * 
 */

/*
 * Weight: http://www.myfitnesspal.com/reports/results/progress/1/7 Neck:
 * http://www.myfitnesspal.com/reports/results/progress/2/7 Waist:
 * http://www.myfitnesspal.com/reports/results/progress/3/7 Hips:
 * http://www.myfitnesspal.com/reports/results/progress/4/7
 * 
 * Fitness:
 * http://www.myfitnesspal.com/reports/results/fitness/Calories%20Burned/7
 * http://www.myfitnesspal.com/reports/results/fitness/Exercise%20Minutes/7
 * 
 * Nutrition: http://www.myfitnesspal.com/reports/results/nutrition/Calories/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Net%20Calories/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Carbs/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Fat/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Protein/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Saturated%20Fat/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Polyunsaturated%20Fat/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Monounsaturated%20Fat/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Trans%20Fat/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Cholesterol/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Sodium/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Potassium/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Fiber/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Sugar/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Vitamin%20A/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Vitamin%20C/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Iron/7
 * http://www.myfitnesspal.com/reports/results/nutrition/Calcium/7
 */
@Entity(name = "Facet_MyFitnessPal")
@ObjectTypeSpec(name = "myfitnesspal", value = 1, isImageType = false, prettyname = "MyFitnessPal")
@NamedQueries({
		@NamedQuery(name = "myfitnesspal.myfitnesspal.all", query = "SELECT facet FROM Facet_MyFitnessPal facet WHERE facet.guestId=? ORDER BY facet.start DESC"),
		@NamedQuery(name = "myfitnesspal.myfitnesspal.deleteAll", query = "DELETE FROM Facet_MyFitnessPal facet WHERE facet.guestId=?"),
		@NamedQuery(name = "myfitnesspal.myfitnesspal.between", query = "SELECT facet FROM Facet_MyFitnessPal facet WHERE facet.guestId=? AND facet.start>=? AND facet.end<=?") })
@Indexed
public class MyFitnessPalFacet extends AbstractFacet {

	public float weight;
	public float neck;
	public float waist;
	public float hips;

	public int caloriesBurned;
	public float exerciseMins;

	public int calories;
	public int netCalories;
	public float carbs;
	public float fat;
	public float protein;
	public float saturatedFat;
	public float polyunsaturatedFat;
	public float monounsaturatedFat;
	public float transFat;
	public float cholesterol;
	public float sodium;
	public float potassium;
	public float fiber;
	public float sugar;
	public float vitaminA;
	public float vitaminC;
	public float iron;
	public float calcium;

	@Override
	protected void makeFullTextIndexable() {
	}

}
