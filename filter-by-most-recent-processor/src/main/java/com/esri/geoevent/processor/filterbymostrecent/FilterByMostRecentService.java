package com.esri.geoevent.processor.filterbymostrecent;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class FilterByMostRecentService extends GeoEventProcessorServiceBase
{
  private Messaging messaging;

  public FilterByMostRecentService()
  {
    definition = new FilterByMostRecentDefinition();
  }

  @Override
  public GeoEventProcessor create() throws ComponentException
  {
    FilterByMostRecent detector = new FilterByMostRecent(definition);
    detector.setMessaging(messaging);
    return detector;
  }

  public void setMessaging(Messaging messaging)
  {
    this.messaging = messaging;
  }
}