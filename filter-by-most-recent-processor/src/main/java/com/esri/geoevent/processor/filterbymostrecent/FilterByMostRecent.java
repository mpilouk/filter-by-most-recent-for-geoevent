package com.esri.geoevent.processor.filterbymostrecent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.esri.ges.core.Uri;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.messaging.EventDestination;
import com.esri.ges.messaging.EventUpdatable;
import com.esri.ges.messaging.GeoEventProducer;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.ges.util.Converter;

public class FilterByMostRecent extends GeoEventProcessorBase implements GeoEventProducer, EventUpdatable
{
	private static final BundleLogger							LOGGER			= BundleLoggerFactory.getLogger(FilterByMostRecent.class);

	private final Map<String, GeoEvent>							trackCache		= new ConcurrentHashMap<String, GeoEvent>();

	private Messaging											messaging;
	private GeoEventProducer									geoEventProducer;
	private Date												clearCacheTime;
	private boolean												autoClearCache;
	private boolean												clearCacheOnStart;
	private Timer												clearCacheTimer;
	private Uri													definitionUri;
	private String												definitionUriString;
	final Object												lock1			= new Object();

	class ClearCacheTask extends TimerTask
	{
		public void run()
		{
			// clear the cache
			if (autoClearCache == true)
			{
				trackCache.clear();
			}
		}
	}

	protected FilterByMostRecent(GeoEventProcessorDefinition definition) throws ComponentException
	{
		super(definition);
	}

	public void afterPropertiesSet()
	{
		String[] resetTimeStr = getProperty("clearCacheTime").getValueAsString().split(":");
		// Get the Date corresponding to 11:01:00 pm today.
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(resetTimeStr[0]));
		calendar.set(Calendar.MINUTE, Integer.parseInt(resetTimeStr[1]));
		calendar.set(Calendar.SECOND, Integer.parseInt(resetTimeStr[2]));
		clearCacheTime = calendar.getTime();
		autoClearCache = Converter.convertToBoolean(getProperty("autoClearCache").getValueAsString());
		clearCacheOnStart = Converter.convertToBoolean(getProperty("clearCacheOnStart").getValueAsString());
	}

	@Override
	public void setId(String id)
	{
		super.setId(id);
		geoEventProducer = messaging.createGeoEventProducer(new EventDestination(id + ":event"));
	}

	@Override
	public GeoEvent process(GeoEvent geoEvent) throws Exception
	{
		String trackId = geoEvent.getTrackId();
		
		// Need to synchronize the Concurrent Map on write to avoid wrong counting
		synchronized (lock1)
		{
			boolean isLatest = false;
			if (trackCache.containsKey(trackId))
			{
				GeoEvent previousGeoEvent = trackCache.get(trackId);
				Date pStartTime = previousGeoEvent.getStartTime();
				Date cStartTime = geoEvent.getStartTime();
				if (pStartTime != null && cStartTime != null)
				{
					isLatest = pStartTime.getTime() < cStartTime.getTime();					
				}				
			}
			else
			{
				isLatest = true;
			}
			if (isLatest == true)
			{
				// Add or update the cache
				trackCache.put(trackId, geoEvent);				
			}
			else
			{
				return null;
			}
		}

		return geoEvent;
	}
	
	@Override
	public List<EventDestination> getEventDestinations()
	{
		return (geoEventProducer != null) ? Arrays.asList(geoEventProducer.getEventDestination()) : new ArrayList<EventDestination>();
	}

	@Override
	public void validate() throws ValidationException
	{
		super.validate();
		List<String> errors = new ArrayList<String>();
		if (errors.size() > 0)
		{
			StringBuffer sb = new StringBuffer();
			for (String message : errors)
				sb.append(message).append("\n");
			throw new ValidationException(LOGGER.translate("VALIDATION_ERROR", this.getClass().getName(), sb.toString()));
		}
	}

	@Override
	public void onServiceStart()
	{
		if (this.clearCacheOnStart == true)
		{
			if (clearCacheTimer == null)
			{
				// Get the Date corresponding to 11:01:00 pm today.
				Calendar calendar1 = Calendar.getInstance();
				calendar1.setTime(clearCacheTime);
				Date time1 = calendar1.getTime();

				clearCacheTimer = new Timer();
				Long dayInMilliSeconds = 60 * 60 * 24 * 1000L;
				clearCacheTimer.scheduleAtFixedRate(new ClearCacheTask(), time1, dayInMilliSeconds);
			}
			trackCache.clear();
		}

		if (definition != null)
		{
			definitionUri = definition.getUri();
			definitionUriString = definitionUri.toString();
		}
	}

	@Override
	public void onServiceStop()
	{
		if (clearCacheTimer != null)
		{
			clearCacheTimer.cancel();
		}
	}

	@Override
	public void shutdown()
	{
		super.shutdown();

		if (clearCacheTimer != null)
		{
			clearCacheTimer.cancel();
		}
	}

	@Override
	public EventDestination getEventDestination()
	{
		return (geoEventProducer != null) ? geoEventProducer.getEventDestination() : null;
	}

	@Override
	public void send(GeoEvent geoEvent) throws MessagingException
	{
		if (geoEventProducer != null && geoEvent != null)
		{
			geoEventProducer.send(geoEvent);
		}
	}

	public void setMessaging(Messaging messaging)
	{
		this.messaging = messaging;
	}

	@Override
	public void disconnect()
	{
		if (geoEventProducer != null)
			geoEventProducer.disconnect();
	}

	@Override
	public String getStatusDetails()
	{
		return (geoEventProducer != null) ? geoEventProducer.getStatusDetails() : "";
	}

	@Override
	public void init() throws MessagingException
	{
		afterPropertiesSet();
	}

	@Override
	public boolean isConnected()
	{
		return (geoEventProducer != null) ? geoEventProducer.isConnected() : false;
	}

	@Override
	public void setup() throws MessagingException
	{
		;
	}

	@Override
	public void update(Observable o, Object arg)
	{
		;
	}
}
