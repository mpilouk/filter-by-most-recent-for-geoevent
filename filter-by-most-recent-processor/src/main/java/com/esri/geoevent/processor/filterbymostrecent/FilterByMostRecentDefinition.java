package com.esri.geoevent.processor.filterbymostrecent;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class FilterByMostRecentDefinition extends GeoEventProcessorDefinitionBase
{
	private static final BundleLogger							LOGGER				= BundleLoggerFactory.getLogger(FilterByMostRecentDefinition.class);

	public FilterByMostRecentDefinition()
	{
		try
		{
			propertyDefinitions.put("clearCacheOnStart", new PropertyDefinition("clearCacheOnStart", PropertyType.Boolean, false, "Clear Cache on Start", "Clear Cache on Start", true, false));
			propertyDefinitions.put("autoClearCache", new PropertyDefinition("autoClearCache", PropertyType.Boolean, false, "Automatic Clear Cache", "Auto Clear Cache", true, false));
			propertyDefinitions.put("clearCacheTime", new PropertyDefinition("clearCacheTime", PropertyType.String, "00:00:00", "Clear cache time", "Clear cache time", "autoClearCache=true", false, false));
		}
		catch (Exception error)
		{
			LOGGER.error("INIT_ERROR", error.getMessage());
			LOGGER.info(error.getMessage(), error);
		}
	}

	@Override
	public String getName()
	{
		return "FilterByMostRecent";
	}

	@Override
	public String getDomain()
	{
		return "com.esri.geoevent.processor";
	}

	@Override
	public String getVersion()
	{
		return "10.4.0";
	}

	@Override
	public String getLabel()
	{
		return "${com.esri.geoevent.processor.filter-by-most-recent-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription()
	{
		return "${com.esri.geoevent.processor.filter-by-most-recent-processor.PROCESSOR_DESC}";
	}
}
