<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<%@ page import="com.percussion.i18n.PSI18nUtils" %>
<%@ page import="com.percussion.i18n.ui.PSI18NTranslationKeyValues" %>
<%@ page import="org.jsecurity.util.StringUtils" %>
<%@ page import="com.percussion.security.SecureStringUtils" %>



<%
	String locale= PSRoleUtilities.getUserCurrentLocale();
	String lang="en";
	if(locale==null){
		locale="en-us";
	}else{
		if(locale.contains("-"))
			lang=locale.split("-")[0];
		else
			lang=locale;
	}
    String debug = request.getParameter("debug");
    boolean isDebug = "true".equals(debug);
    String debugQueryString = isDebug ? "?debug=true" : "";
    String site = request.getParameter("site");
    if (site == null)
        site = "";
    //Checking for vulnerability
    if(!SecureStringUtils.isValidString(site)){
        response.sendError(response.SC_FORBIDDEN, "Invalid Site Name!");
    }
    Boolean hasSites = (Boolean) request.getAttribute("hasSites");
    String inlineHelpMsg = hasSites != null && hasSites
            ? PSI18nUtils.getString("perc.ui.site.architecture@Work On Navigation", locale)
            : PSI18nUtils.getString("perc.ui.site.architecture@Click Create Site To Create Site", locale);
%>
<i18n:settings lang="<%=locale %>" prefixes="perc.ui." debug="<%= debug %>"/>
<!DOCTYPE html>
<html lang="<%=lang %>">
<head>
<title><i18n:message key="perc.ui.architecture.title@Architecture"/></title>
<!--Meta Includes -->
<meta name="viewport" content="width=device-width, initial-scale=1">
<%@include file="includes/common_meta.jsp" %>

<%--
  When ran in normal mode all javascript will be in one compressed file and
  the same for css (Currently just concatenated bu tnot compressed.).
  To run from the non-compressed file simply add the query string param
  ?debug=true to the url for the page.

  Be sure that when a new javascript file is added to the page, an entry
  for each inclusion will be needed in the appropriate concat task within
  the minify target in the build.xml file. If this is not done then it won't
  get into the files used in production.
--%>

    <!-- Themes/skin never should be concatenated or packed -->
    <link rel="stylesheet" type="text/css" href="../themes/smoothness/jquery-ui-1.8.9.custom.css"/>
    <link rel='stylesheet' type='text/css' href='../css/dynatree/skin/ui.dynatree.css'/>
    <link rel="stylesheet" type="text/css" href="/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=<%=locale%>"></script>
    <script src="/JavaScriptServlet"></script>
<% if (isDebug) { %>

<!-- CSS Includes -->
<%@include file="includes/common_css.jsp" %>
<link type="text/css" href="../css/perc_mcol.css" rel="stylesheet"/>
<link rel="stylesheet" type="text/css" href="../css/styles.css"/>
<link rel="stylesheet" type="text/css" href="../css/perc_site_map.css"/>
<link rel="stylesheet" type="text/css" href="../css/perc_newSectionDialog.css"/>
<link rel="stylesheet" type="text/css" href="../css/perc_newsitedialog.css"/>
<link rel="stylesheet" type="text/css" href="../css/perc_new_page_button.css"/>
<link rel="stylesheet" type="text/css" href="../css/perc_save_as_dialog.css"/>
<link rel="stylesheet" type="text/css" href="../css/perc_ChangePw.css"/>
<!-- JavaScript Includes (order matters) -->
<%@include file="includes/common_js.jsp" %>
<%@include file="includes/finder_js.jsp" %>
<script src="../services/perc_sectionServiceClient.js"></script>
<script src="../services/PercSiteService.js"></script>
<script src="../plugins/PercEditSectionLinksDialog.js"></script>
<script src="../plugins/PercSectionTreeDialog.js"></script>
<script src="../plugins/PercCopySiteDialog.js"></script>
<script src="../services/PercFolderService.js"></script>
<script src="../plugins/perc_newSectionDialog.js"></script>
<script src="../plugins/perc_editSectionDialog.js"></script>
<script src="../plugins/perc_editSiteSectionDialog.js"></script>
<script src="../widgets/perc_site_map.js"></script>
<script src="../widgets/perc_save_as.js"></script>
<script src="../plugins/perc_ChangePwDialog.js"></script>
<script src="../plugins/perc_ChangeUserEmailDialog.js"></script>
<script src="../plugins/perc_newsitedialog.js"></script>
<% } else { %>
<link rel="stylesheet" type="text/css" href="../cssMin/perc_architecture.packed.min.css"/>
<script src="../jslibMin/perc_architecture.packed.min.js"></script>
<% } %>
<script>

    $(function () {

        <% if(!site.equals("")){ %>
        var siteArchUrl = "<%=site%>";

        $("#perc_site_map").perc_site_map({
            site: siteArchUrl,
            onChange: function () {
                $.perc_finder().refresh();
            }
        });
        <%} else { %>
        $("#perc_site_map").html("<div style='height: 10px;'></div><div id='perc-site-templates-inline-help'><%=inlineHelpMsg%></div>");
        <%}%>
        $.Percussion.PercFinderView();
    });
</script>
</head>
<body view="PERC_SITE" style="overflow:auto">
<div class="perc-main perc-finder-fix" style="position:fixed">
    <div class="perc-header">
        <jsp:include page="includes/header.jsp" flush="true">
            <jsp:param name="mainNavTab" value="architecture"/>
        </jsp:include>
    </div>

    <div class="ui-layout-north" style="padding: 0 0; overflow:visible">
        <jsp:include page="includes/finder.jsp" flush="true">
            <jsp:param name="openedObject" value="PERC_SITE"/>
        </jsp:include>
    </div>
</div>

<div id="perc_sa_container" class="ui-widget ui-corner-all">
    <!--Architecture Map Goes Here-->
    <div id="perc_site_map">
        <div id="perc-navigation-menu-wrapper" style="z-index: 4460;">
            <div id="perc-navigation-menu">
            </div>
        </div>
        <div class="ui-helper-clearfix" id="perc-pageEditor-toolbar-content"></div>
    </div>
</div>
<%@include file='includes/siteimprove_integration.html'%>
</body>
</html>
