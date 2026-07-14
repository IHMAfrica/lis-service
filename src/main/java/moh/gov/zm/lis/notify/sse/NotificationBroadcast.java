package moh.gov.zm.lis.notify.sse;

import java.util.List;
import java.util.UUID;
import lombok.Generated;
import moh.gov.zm.lis.notify.dto.NotificationDTO;

public class NotificationBroadcast {
   private List<UUID> userIds;
   private NotificationDTO.NotificationResponse notification;

   @Generated
   public List<UUID> getUserIds() {
      return this.userIds;
   }

   @Generated
   public NotificationDTO.NotificationResponse getNotification() {
      return this.notification;
   }

   @Generated
   public void setUserIds(final List<UUID> userIds) {
      this.userIds = userIds;
   }

   @Generated
   public void setNotification(final NotificationDTO.NotificationResponse notification) {
      this.notification = notification;
   }

   @Generated
   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof NotificationBroadcast)) {
         return false;
      } else {
         NotificationBroadcast other = (NotificationBroadcast)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$userIds = this.getUserIds();
            Object other$userIds = other.getUserIds();
            if (this$userIds == null) {
               if (other$userIds != null) {
                  return false;
               }
            } else if (!this$userIds.equals(other$userIds)) {
               return false;
            }

            Object this$notification = this.getNotification();
            Object other$notification = other.getNotification();
            if (this$notification == null) {
               if (other$notification != null) {
                  return false;
               }
            } else if (!this$notification.equals(other$notification)) {
               return false;
            }

            return true;
         }
      }
   }

   @Generated
   protected boolean canEqual(final Object other) {
      return other instanceof NotificationBroadcast;
   }

   @Generated
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $userIds = this.getUserIds();
      result = result * 59 + ($userIds == null ? 43 : $userIds.hashCode());
      Object $notification = this.getNotification();
      result = result * 59 + ($notification == null ? 43 : $notification.hashCode());
      return result;
   }

   @Generated
   public String toString() {
      String var10000 = String.valueOf(this.getUserIds());
      return "NotificationBroadcast(userIds=" + var10000 + ", notification=" + String.valueOf(this.getNotification()) + ")";
   }

   @Generated
   public NotificationBroadcast() {
   }

   @Generated
   public NotificationBroadcast(final List<UUID> userIds, final NotificationDTO.NotificationResponse notification) {
      this.userIds = userIds;
      this.notification = notification;
   }
}
