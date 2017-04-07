<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/aps-core" prefix="wp" %>
<%@ taglib prefix="wpsf" uri="/apsadmin-form" %>
<%@ taglib uri="/apsadmin-core" prefix="wpsa" %>

<ol class="breadcrumb page-tabs-header breadcrumb-position">
	<li><a href="<s:url namespace="/do/BaseAdmin" action="settings" />"><s:text name="menu.configure"/></a></li>
	<li>
		<a href="<s:url namespace="/do/LocaleString" action="list" />">
			<s:text name="title.languageAndLabels" />
		</a>
	</li>
	<li class="page-title-container">
		<s:if test="getStrutsAction() == 1"><s:text name="title.generalSettings.locale.new" /></s:if>
		<s:elseif test="getStrutsAction() == 2"><s:text name="title.generalSettings.locale.edit" /> </s:elseif>
	</li>
</ol>
<h1 class="page-title-container">
	<div>
		<s:if test="getStrutsAction() == 1"><s:text name="title.generalSettings.locale.new" /></s:if>
		<s:elseif test="getStrutsAction() == 2"><s:text name="title.generalSettings.locale.edit" /> </s:elseif>
		<span class="pull-right">
            <a tabindex="0" role="button" data-toggle="popover" data-trigger="focus" data-html="true" title=""
			   data-content="TO be inserted" data-placement="left" data-original-title="">
                <i class="fa fa-question-circle-o" aria-hidden="true"></i>
            </a>
        </span>
	</div>
</h1>
<div class="text-right">
	<div class="form-group-separator"></div>
</div>
<br>

<s:form action="save" namespace="/do/LocaleString" cssClass="form-horizontal">
	<div class="pull-right"><s:text name="label.default" /></div>
	<s:if test="hasFieldErrors()">
		<div class="alert alert-danger">
			<button class="close" data-dismiss="alert"><span class="icon fa fa-times"></span></button>
			<h2 class="h4 margin-none">
				<s:text name="message.title.FieldErrors" />
			</h2>
				<%--
                    <ul class="margin-base-top">
                    <s:iterator value="fieldErrors">
                        <s:iterator value="value">
                        <li><s:property escapeHtml="false" /></li>
                        </s:iterator>
                    </s:iterator>
                    </ul>
                --%>
		</div>
	</s:if>
	<p class="sr-only">
		<wpsf:hidden value="%{getStrutsAction()}" name="strutsAction"/>
		<s:if test="getStrutsAction() == 2">
			<wpsf:hidden value="%{key}" name="key" />
		</s:if>
	</p>
	<s:set var="keyFieldErrorsVar" value="%{fieldErrors['key']}"/>
	<s:set var="keyHasFieldErrorVar" value="#keyFieldErrorsVar != null && !#keyFieldErrorsVar.isEmpty()"/>
	<s:set var="controlGroupErrorClassVar" value="%{#keyHasFieldErrorVar ? ' has-error' : ''}"/>

	<div class="form-group<s:property value="#controlGroupErrorClassVar" />">
		<label class="col-sm-2 control-label" for="editLabel_key"><s:text name="label.code"/></label>
		<div class="col-sm-10">
			<wpsf:textfield value="%{key}" name="key" id="editLabel_key" disabled="%{getStrutsAction() == 2}" cssClass="form-control"/>
			<s:if test="#keyHasFieldErrorVar">
				<p class="text-danger padding-small-vertical"><s:iterator value="#keyFieldErrorsVar"><s:property/>&#32;</s:iterator></p>
			</s:if>
		</div>
	</div>

	<s:iterator value="langs" var="l">
		<s:if test="#l.default">
			<s:set var="currentFieldErrorsVar" value="%{fieldErrors[#l.code]}"/>
			<s:set var="currentHasFieldErrorVar" value="#currentFieldErrorsVar != null && !#currentFieldErrorsVar.isEmpty()"/>
			<s:set var="controlGroupErrorClassVar" value="%{#currentHasFieldErrorVar ? ' has-error' : ''}"/>
			<div class="form-group<s:property value="#controlGroupErrorClassVar" />">
				<label class="col-sm-2 control-label" for="lang<s:property value="#l.code"/>">
						<%-- (<s:property value="#l.code" />)&#32;<s:text name="label.description" /> --%>
					<span lang="<s:property value="#l.code" />"><s:property value="#l.descr"/></span>*
				</label>
				<div class="col-sm-10">
					<s:textarea cols="50" rows="3" name="%{code}" id="%{'lang'+code}" value="%{labels[#l.code]}"
								cssClass="form-control"/>
					<s:if test="#currentHasFieldErrorVar">
						<p class="text-danger padding-small-vertical"><s:iterator
								value="#currentFieldErrorsVar"><s:property/>&#32;</s:iterator></p>
					</s:if>
				</div>
			</div>
		</s:if>
	</s:iterator>

	<s:if test="%{langs.size() > 1}">
		<s:iterator value="langs" var="l">
			<s:if test="! #l.default">
				<s:set var="currentFieldErrorsVar" value="%{fieldErrors[#l.code]}"/>
				<s:set var="currentHasFieldErrorVar"
					   value="#currentFieldErrorsVar != null && !#currentFieldErrorsVar.isEmpty()"/>
				<s:set var="controlGroupErrorClassVar" value="%{#currentHasFieldErrorVar ? ' has-error' : ''}"/>
				<div class="form-group<s:property value="#controlGroupErrorClassVar" />">
					<label class="col-sm-2 control-label" for="lang<s:property value="#l.code"/>">
							<%-- (<s:property value="#l.code" />)&#32;<s:text name="label.description" /> --%>
						<span lang="<s:property value="#l.code" />"><s:property value="#l.descr"/></span>
					</label>
					<div class="col-sm-10">
						<s:textarea cols="50" rows="3" name="%{code}" id="%{'lang'+code}" value="%{labels[#l.code]}"
									cssClass="form-control"/>
						<s:if test="#currentHasFieldErrorVar">
							<p class="text-danger padding-small-vertical"><s:iterator
									value="#currentFieldErrorsVar"><s:property/>&#32;</s:iterator></p>
						</s:if>
					</div>
				</div>
			</s:if>
		</s:iterator>
	</s:if>

	<div class="form-group">
		<div class="col-sm-offset-2 col-sm-10">
			<wpsf:submit type="button" action="save" cssClass="btn btn-primary btn-lg pull-right">
				<s:text name="label.save"/>
			</wpsf:submit>
		</div>
	</div>
</s:form>
