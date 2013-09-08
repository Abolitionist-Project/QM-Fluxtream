package com.fluxtream.test.services.variables;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import com.fluxtream.dto.GroupingDto;
import com.fluxtream.dto.VariableDto;
import com.fluxtream.services.VariablesService;
import com.fluxtream.services.exceptions.BaseServiceException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext-test.xml")
public class VariablesServiceTest extends TestCase {

	@Resource
	VariablesService variablesService;

	@Test
	public void testGetVariable() throws Exception {
		Assert.notNull(variablesService.getVariable(null, 1));
	}

	@Test(expected = BaseServiceException.class)
	public void testNotExistedVariable() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, -1, new DateTime(2013, 1, 5, 9, 0, 0, 0),
				new DateTime(2013, 10, 30, 11, 0, 0, 0), GroupingDto.HOURLY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.isTrue(groupedValues.isEmpty());
	}

	@Test
	public void testEmptyValuesInRange() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 3, new DateTime(2013, 2, 5, 9, 0, 0, 0),
				new DateTime(2013, 1, 30, 11, 0, 0, 0), GroupingDto.HOURLY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.isTrue(groupedValues.isEmpty());
	}

	@Test
	public void testVariableValues() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 3, new DateTime(2013, 4, 5, 9, 0, 0, 0),
				new DateTime(2013, 5, 30, 11, 0, 0, 0), GroupingDto.HOURLY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.notEmpty(groupedValues);
		Map<Long, Double> expectedValues = new HashMap<Long, Double>();
		expectedValues.put(new DateTime(2013, 4, 13, 9, 0, 0, 0).getMillis(), 40.0);
		assertEqualsMaps(groupedValues, expectedValues);
	}

	@Test
	public void testPrioritizedValuesGroupedByHoursInTimeRange() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 1, new DateTime(2013, 4, 20, 9, 0, 0, 0),
				new DateTime(2013, 5, 30, 11, 0, 0, 0), GroupingDto.HOURLY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.notEmpty(groupedValues);
		Map<Long, Double> expectedValues = new HashMap<Long, Double>();
		expectedValues.put(new DateTime(2013, 5, 30, 10, 0, 0, 0).getMillis(), 5.0);
		expectedValues.put(new DateTime(2013, 4, 24, 10, 0, 0, 0).getMillis(), 0.0);
		expectedValues.put(new DateTime(2013, 4, 25, 10, 0, 0, 0).getMillis(), 10.0);
		assertEqualsMaps(groupedValues, expectedValues);
	}

	@Test
	public void testPrioritizedValuesGroupedByHours() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 1, new DateTime(2013, 4, 13, 9, 0, 0, 0),
				new DateTime(2013, 5, 30, 11, 0, 0, 0), GroupingDto.HOURLY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.notEmpty(groupedValues);
		Map<Long, Double> expectedValues = new HashMap<Long, Double>();
		expectedValues.put(new DateTime(2013, 5, 30, 10, 0, 0, 0).getMillis(), 5.0);
		expectedValues.put(new DateTime(2013, 4, 24, 10, 0, 0, 0).getMillis(), 0.0);
		expectedValues.put(new DateTime(2013, 4, 13, 10, 0, 0, 0).getMillis(), 62.5);
		expectedValues.put(new DateTime(2013, 4, 25, 10, 0, 0, 0).getMillis(), 10.0);
		expectedValues.put(new DateTime(2013, 4, 14, 10, 0, 0, 0).getMillis(), 5.0);
		expectedValues.put(new DateTime(2013, 4, 13, 9, 0, 0, 0).getMillis(), 10.0);
		assertEqualsMaps(groupedValues, expectedValues);
	}

	@Test
	public void testPrioritizedValuesGroupedByDays() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 1, new DateTime(2013, 4, 13, 9, 0, 0, 0),
				new DateTime(2013, 5, 30, 11, 0, 0, 0), GroupingDto.DAILY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.notEmpty(groupedValues);
		Map<Long, Double> expectedValues = new HashMap<Long, Double>();
		expectedValues.put(new DateTime(2013, 5, 30, 0, 0, 0, 0).getMillis(), 5.0);
		expectedValues.put(new DateTime(2013, 4, 24, 0, 0, 0, 0).getMillis(), 0.0);
		expectedValues.put(new DateTime(2013, 4, 13, 0, 0, 0, 0).getMillis(), 72.5);
		expectedValues.put(new DateTime(2013, 4, 14, 0, 0, 0, 0).getMillis(), 5.0);
		expectedValues.put(new DateTime(2013, 4, 25, 0, 0, 0, 0).getMillis(), 10.0);
		assertEqualsMaps(groupedValues, expectedValues);
	}

	@Test
	public void testPrioritizedValuesGroupedByWeeks() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 1, new DateTime(2013, 4, 13, 9, 0, 0, 0),
				new DateTime(2013, 5, 30, 11, 0, 0, 0), GroupingDto.WEEKLY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.notEmpty(groupedValues);
		Map<Long, Double> expectedValues = new HashMap<Long, Double>();
		expectedValues.put(new DateTime(2013, 4, 22, 0, 0, 0, 0).getMillis(), 10.0);
		expectedValues.put(new DateTime(2013, 4, 8, 0, 0, 0, 0).getMillis(), 77.5);
		expectedValues.put(new DateTime(2013, 5, 27, 0, 0, 0, 0).getMillis(), 5.0);
		assertEqualsMaps(groupedValues, expectedValues);
	}

	@Test
	public void testPrioritizedValuesGroupedByMonths() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 1, new DateTime(2013, 4, 13, 9, 0, 0, 0),
				new DateTime(2013, 5, 30, 11, 0, 0, 0), GroupingDto.MONTHLY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.notEmpty(groupedValues);
		Map<Long, Double> expectedValues = new HashMap<Long, Double>();
		expectedValues.put(new DateTime(2013, 5, 1, 0, 0, 0, 0).getMillis(), 5.0);
		expectedValues.put(new DateTime(2013, 4, 1, 0, 0, 0, 0).getMillis(), 87.5);
		assertEqualsMaps(groupedValues, expectedValues);
	}

	@Test
	public void testRemainingValuesGroupedByDays() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 2, new DateTime(2013, 4, 13, 9, 0, 0, 0),
				new DateTime(2013, 5, 30, 11, 0, 0, 0), GroupingDto.DAILY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.notEmpty(groupedValues);
		Map<Long, Double> expectedValues = new HashMap<Long, Double>();
		expectedValues.put(new DateTime(2013, 5, 30, 0, 0, 0, 0).getMillis(), 25.0);
		expectedValues.put(new DateTime(2013, 4, 13, 0, 0, 0, 0).getMillis(), 14.5);
		expectedValues.put(new DateTime(2013, 4, 14, 0, 0, 0, 0).getMillis(), 25.0);
		expectedValues.put(new DateTime(2013, 4, 25, 0, 0, 0, 0).getMillis(), 10.0);
		assertEqualsMaps(groupedValues, expectedValues);
	}

	@Test
	public void testRemainingValuesGroupedByHoursWithTimeShift() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 4, new DateTime(2013, 4, 9, 9, 0, 0, 0),
				new DateTime(2013, 5, 30, 11, 0, 0, 0), GroupingDto.HOURLY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.notEmpty(groupedValues);
		Map<Long, Double> expectedValues = new HashMap<Long, Double>();
		expectedValues.put(new DateTime(2013, 4, 14, 20, 0, 0, 0).getMillis(), 25.0);
		expectedValues.put(new DateTime(2013, 4, 13, 19, 0, 0, 0).getMillis(), 11.25);
		expectedValues.put(new DateTime(2013, 4, 13, 20, 0, 0, 0).getMillis(), 15.625);
		expectedValues.put(new DateTime(2013, 4, 25, 20, 0, 0, 0).getMillis(), 10.0);
		expectedValues.put(new DateTime(2013, 5, 30, 20, 0, 0, 0).getMillis(), 25.0);
		assertEqualsMaps(groupedValues, expectedValues);
	}

	@Test
	public void testRemainingValuesGroupedByHoursWithMinusTimeShift() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 7, new DateTime(2013, 4, 9, 9, 0, 0, 0),
				new DateTime(2013, 5, 30, 11, 0, 0, 0), GroupingDto.HOURLY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.notEmpty(groupedValues);
		Map<Long, Double> expectedValues = new HashMap<Long, Double>();
		expectedValues.put(new DateTime(2013, 5, 30, 9, 0, 0, 0).getMillis(), 5.0);
		expectedValues.put(new DateTime(2013, 4, 14, 9, 0, 0, 0).getMillis(), 5.0);
		expectedValues.put(new DateTime(2013, 4, 13, 8, 0, 0, 0).getMillis(), 2.5);
		expectedValues.put(new DateTime(2013, 4, 13, 9, 0, 0, 0).getMillis(), 6.25);
		assertEqualsMaps(groupedValues, expectedValues);
	}

	@Test
	public void testRemainingValuesGroupedByWeeksWithTimeShift() throws Exception {
		VariableDto variableDto = variablesService.getVariableValues(null, 4, new DateTime(2013, 4, 9, 9, 0, 0, 0),
				new DateTime(2013, 5, 30, 11, 0, 0, 0), GroupingDto.WEEKLY);
		Map<Long, Double> groupedValues = variableDto.getGroupedValues();
		Assert.notEmpty(groupedValues);
		Map<Long, Double> expectedValues = new HashMap<Long, Double>();
		expectedValues.put(new DateTime(2013, 4, 22, 0, 0, 0, 0).getMillis(), 10.0);
		expectedValues.put(new DateTime(2013, 4, 8, 0, 0, 0, 0).getMillis(), 15.714);
		expectedValues.put(new DateTime(2013, 5, 27, 0, 0, 0, 0).getMillis(), 25.0);
		assertEqualsMaps(groupedValues, expectedValues);
	}

	private void assertEqualsMaps(Map<Long, Double> groupedValues, Map<Long, Double> expectedValues) {

		Assert.isTrue(expectedValues.size() == groupedValues.size());
		for (Map.Entry<Long, Double> entry : expectedValues.entrySet()) {
			Long key = entry.getKey();
			Assert.isTrue(groupedValues.containsKey(key));
			assertEquals(entry.getValue(), groupedValues.get(key), 0.001);
		}
	}
}
