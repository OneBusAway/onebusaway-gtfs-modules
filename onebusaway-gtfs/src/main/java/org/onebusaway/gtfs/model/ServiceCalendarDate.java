package org.onebusaway.gtfs.model;

import org.onebusaway.gtfs.model.calendar.ServiceDate;

/**
 * @author bdferris
 * 
 */
public final class ServiceCalendarDate extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  public static final int EXCEPTION_TYPE_ADD = 1;

  public static final int EXCEPTION_TYPE_REMOVE = 2;

  private int id;

  private AgencyAndId serviceId;

  private ServiceDate date;

  private int exceptionType;

  public ServiceCalendarDate() {

  }

  public ServiceCalendarDate(ServiceCalendarDate obj) {
    this.id = obj.id;
    this.serviceId = obj.serviceId;
    this.date = obj.date;
    this.exceptionType = obj.exceptionType;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public AgencyAndId getServiceId() {
    return serviceId;
  }

  public void setServiceId(AgencyAndId serviceId) {
    this.serviceId = serviceId;
  }

  public ServiceDate getDate() {
    return date;
  }

  public void setDate(ServiceDate date) {
    this.date = date;
  }

  public int getExceptionType() {
    return exceptionType;
  }

  public void setExceptionType(int exceptionType) {
    this.exceptionType = exceptionType;
  }

  @Override
  public String toString() {
    return "<CalendarDate serviceId=" + this.serviceId + " date=" + this.date
        + " exception=" + this.exceptionType + ">";
  }
}
