/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.powershell.model;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.StatisticType;
import com.redhat.rhevm.api.model.StatisticUnit;
import com.redhat.rhevm.api.model.ValueType;

public class PowerShellVmStatisticsParserTest extends PowerShellModelTest {

    @Test
    public void testParseStats() throws Exception {
        String data = readFileContents("vmStats.xml");
        assertNotNull(data);

        List<Statistic> statistics = PowerShellVmStatisticsParser.parse(getParser(), data);
        assertNotNull(statistics);
        assertEquals(4, statistics.size());
        verifyStatistic(statistics.get(0),
                        "memory.installed",
                        "Total mem configured",
                        StatisticType.GAUGE,
                        StatisticUnit.BYTES,
                        ValueType.INTEGER,
                        512*1024*1024L);
        verifyStatistic(statistics.get(1),
                        "memory.used",
                        "Memory used (agent)",
                        StatisticType.GAUGE,
                        StatisticUnit.BYTES,
                        ValueType.INTEGER,
                        512*1024*1024L/2);
        verifyStatistic(statistics.get(2),
                        "cpu.current.guest",
                        "CPU used by guest",
                        StatisticType.GAUGE,
                        StatisticUnit.PERCENT,
                        ValueType.DECIMAL,
                        new BigDecimal("33.5"));
        verifyStatistic(statistics.get(3),
                        "cpu.current.hypervisor",
                        "CPU overhead",
                        StatisticType.GAUGE,
                        StatisticUnit.PERCENT,
                        ValueType.DECIMAL,
                        new BigDecimal("21.7"));
    }

    private void verifyStatistic(Statistic statistic,
                                 String name,
                                 String description,
                                 StatisticType type,
                                 StatisticUnit unit,
                                 ValueType valueType,
                                 long datum) {
        verifyStatistic(statistic, name, description, type, unit, valueType, new BigDecimal(datum));
    }

    private void verifyStatistic(Statistic statistic,
                                 String name,
                                 String description,
                                 StatisticType type,
                                 StatisticUnit unit,
                                 ValueType valueType,
                                 BigDecimal datum) {
        assertEquals(name, statistic.getName());
        assertEquals(description, statistic.getDescription());
        assertEquals(type, statistic.getType());
        assertEquals(unit, statistic.getUnit());
        assertTrue(statistic.isSetValues());
        assertEquals(valueType, statistic.getValues().getType());
        assertTrue(statistic.getValues().isSetValues());
        assertEquals(1, statistic.getValues().getValues().size());
        assertEquals(datum, statistic.getValues().getValues().get(0).getDatum());
    }
}

