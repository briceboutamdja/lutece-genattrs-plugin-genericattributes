package fr.paris.lutece.plugins.genericattributes.service.entrytype;

import fr.paris.lutece.plugins.genericattributes.business.*;
import fr.paris.lutece.plugins.genericattributes.util.GenericAttributesUtils;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

public abstract class AbstractEntryTypeSlot extends EntryTypeService
{
    // PARAMETERS
    public static final String PARAMETER_SUFFIX_BEGIN = "_begin";
    public static final String PARAMETER_SUFFIX_END   = "_end";

    String FIELD_BEGIN_HOUR = "begin_hour";
    String FIELD_END_HOUR   = "end_hour";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestData( Entry entry, HttpServletRequest request, Locale locale )
    {
        initCommonRequestData( entry, request );
        String strTitle = request.getParameter( PARAMETER_TITLE );
        String strCode = request.getParameter( PARAMETER_ENTRY_CODE );
        String strHelpMessage = ( request.getParameter( PARAMETER_HELP_MESSAGE ) != null ) ? request.getParameter( PARAMETER_HELP_MESSAGE ).trim( ) : null;
        String strComment = request.getParameter( PARAMETER_COMMENT );
        String strMandatory = request.getParameter( PARAMETER_MANDATORY );
        String strOnlyDisplayInBack = request.getParameter( PARAMETER_ONLY_DISPLAY_IN_BACK );
        String strCSSClass = request.getParameter( PARAMETER_CSS_CLASS );
        String strIndexed = request.getParameter( PARAMETER_INDEXED );

        String strFieldError = StringUtils.EMPTY;

        if ( StringUtils.isBlank( strTitle ) )
        {
            strFieldError = ERROR_FIELD_TITLE;
        }

        if ( StringUtils.isNotBlank( strFieldError ) )
        {
            Object[] tabRequiredFields = { I18nService.getLocalizedString( strFieldError, locale ) };

            return AdminMessageService.getMessageUrl( request, MESSAGE_MANDATORY_FIELD, tabRequiredFields, AdminMessage.TYPE_STOP );
        }

        GenericAttributesUtils.createOrUpdateField( entry, FIELD_BEGIN_HOUR, null, FIELD_BEGIN_HOUR );
        GenericAttributesUtils.createOrUpdateField( entry, FIELD_END_HOUR, null, FIELD_END_HOUR );

        entry.setTitle( strTitle );
        entry.setCode( strCode );
        entry.setHelpMessage( strHelpMessage );
        entry.setComment( strComment );
        entry.setCSSClass( strCSSClass );
        entry.setIndexed( strIndexed != null );
        entry.setMandatory( strMandatory != null );
        entry.setOnlyDisplayInBack( strOnlyDisplayInBack != null );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericAttributeError getResponseData( Entry entry, HttpServletRequest request, List<Response> listResponse, Locale locale )
    {
        String strBeginHour = request.getParameter( getAttributeName( entry, request ) + PARAMETER_SUFFIX_BEGIN );
        String strEndHour = request.getParameter( getAttributeName( entry, request ) + PARAMETER_SUFFIX_END );

        if ( strBeginHour != null && strEndHour != null )
        {
            Field fieldBeginHour = entry.getFieldByCode( FIELD_BEGIN_HOUR );
            Field fieldEndHour = entry.getFieldByCode( FIELD_END_HOUR );

            if ( fieldBeginHour == null )
            {
                fieldBeginHour = GenericAttributesUtils.createOrUpdateField( entry, FIELD_BEGIN_HOUR, null, FIELD_BEGIN_HOUR );
                FieldHome.create( fieldBeginHour );
            }

            if ( fieldEndHour == null )
            {
                fieldEndHour = GenericAttributesUtils.createOrUpdateField( entry, FIELD_END_HOUR, null, FIELD_END_HOUR );
                FieldHome.create( fieldEndHour );
            }

            listResponse.add( createResponse( strBeginHour, entry, fieldBeginHour, request ) );
            listResponse.add( createResponse( strEndHour, entry, fieldEndHour, request ) );
        }

        if ( entry.isMandatory( ) )
        {
            return new MandatoryError( entry, locale );
        } else if ( StringUtils.isBlank( strBeginHour ) || StringUtils.isBlank( strEndHour ) )
        {
            GenericAttributeError error = new GenericAttributeError( );
            error.setMandatoryError( entry.isMandatory( ) );
            error.setTitleQuestion( entry.getTitle( ) );
            error.setErrorMessage( I18nService.getLocalizedString( MESSAGE_ERROR_SLOT, locale ) );
            return error;
        } else if ( LocalTime.parse( strEndHour ).isBefore( LocalTime.parse( strBeginHour ) ) )
        {
            GenericAttributeError error = new GenericAttributeError( );
            error.setMandatoryError( entry.isMandatory( ) );
            error.setTitleQuestion( entry.getTitle( ) );
            error.setErrorMessage( I18nService.getLocalizedString( MESSAGE_ERROR_IMPOSSIBLE_SLOT, locale ) );
            return error;
        } else
        {
            return super.getResponseData( entry, request, listResponse, locale );
        }
    }

    protected Response createResponse( String responseValue, Entry entry, Field field, HttpServletRequest request )
    {
        Response response = new Response( );

        response.setEntry( entry );
        response.setResponseValue( responseValue );
        response.setField( field );
        response.setToStringValueResponse( responseValue );
        response.setIterationNumber( getResponseIterationValue( request ) );

        return response;
    }

    /**
     * Gives the attribute name
     *
     * @param entry
     *         the entry
     * @param request
     *         the request
     * @return the attribute name
     */
    protected String getAttributeName( Entry entry, HttpServletRequest request )
    {
        return PREFIX_ATTRIBUTE + entry.getIdEntry( );
    }
}
