/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.rest.pages;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "CalendarInfo")
@Schema(name="CalendarInfo",description="Represents Calendar information.")
public class CalendarInfo
{
    @Schema(name="startDate", required=false,description="Starting Date.")
    private Date startDate;

    @Schema(name="endDate", required=false,description="Ending Date.")
    private Date endDate;

    @Schema(name="calendars", required=false,description="List of calendars.")
    private List<String> calendars;

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public List<String> getCalendars()
    {
        return calendars;
    }

    public void setCalendars(List<String> calendars)
    {
        this.calendars = calendars;
    }

    @Override
    public String toString()
    {
        return "DateInfo [startDate=" + startDate + ", endDate=" + endDate + ", Calendars=" + calendars + "]";
    }

}
