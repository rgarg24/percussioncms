<?xml version='1.0' encoding='UTF-8'?>
<!--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  -->

<!DOCTYPE xsl:stylesheet [
<!ENTITY % HTMLlat1 SYSTEM "file:../../DTD/HTMLlat1x.ent">
		%HTMLlat1;
	<!ENTITY % HTMLsymbol SYSTEM "file:../../DTD/HTMLsymbolx.ent">
		%HTMLsymbol;
	<!ENTITY % HTMLspecial SYSTEM "file:../../DTD/HTMLspecialx.ent">
		%HTMLspecial;
]>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >
  <xsl:template match="*" mode="rolelist">
  		<tr>
		  <td colspan="10" class="headercellfont" align="right">
			 <a href="{newlink}">New Role</a>
		  </td>
		</tr>
     <tr class="headercell2">
        <td height="21" width="5%" align="center" class="headercell2font">&nbsp;</td>
        <td height="21" width="45%" align="left" class="headercell2font">Name (ID)</td>
        <td height="21" width="50%" align="left" class="headercell2font">Description</td>
      </tr>
    <xsl:for-each select="role">
     <xsl:if test="roleid != ''">
      <tr class="datacell1">
			<xsl:attribute name="class"> 
				<xsl:choose> 
					<xsl:when test="position() mod 2 = 1"> 
						<xsl:value-of select="'datacell1'"/> 
					</xsl:when> 
					<xsl:otherwise> 
						<xsl:value-of select="'datacell2'"/> 
					</xsl:otherwise> 
				</xsl:choose> 
			</xsl:attribute> 
        <td class="datacell1font" height="21" align="center">
            <a>
              <xsl:attribute name="href">javascript: delConfirm('<xsl:value-of select="deletelink"/>' );</xsl:attribute>
              <img src="../sys_resources/images/delete.gif" width="21" height="21" border="0"/>
            </a>
        </td>
        <td class="datacell1font" height="21" align="left">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="link"/>
              </xsl:attribute>
					<xsl:apply-templates select="name"/>&nbsp;(<xsl:apply-templates select="roleid"/>)&nbsp;
            </a>
        </td>

        <td class="datacell1font" height="21">
				<xsl:apply-templates select="roledesc"/>&nbsp;
        </td>

      </tr>
</xsl:if>
	  <xsl:if test="count(.)=1 and roleid = ''">
		<tr class="datacell1">
			<td align="center" colspan="3" class="datacellnoentriesfound">
				No entries found.&nbsp;
			</td>
      </tr>
	  </xsl:if>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
