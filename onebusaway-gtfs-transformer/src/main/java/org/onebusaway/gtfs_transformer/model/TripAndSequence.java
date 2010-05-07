/**
 * 
 */
package org.onebusaway.gtfs_transformer.model;

public class TripAndSequence implements Comparable<TripAndSequence> {

  private String _tripId;

  private int _sequence;

  public TripAndSequence(String tripId, int sequence) {
    _tripId = tripId;
    _sequence = sequence;
  }

  @Override
  public int compareTo(TripAndSequence o) {
    return _sequence - o._sequence;
  }

  public String getTripId() {
    return _tripId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sequence;
    result = prime * result + ((_tripId == null) ? 0 : _tripId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TripAndSequence other = (TripAndSequence) obj;
    if (_sequence != other._sequence)
      return false;
    if (_tripId == null) {
      if (other._tripId != null)
        return false;
    } else if (!_tripId.equals(other._tripId))
      return false;
    return true;
  }
}