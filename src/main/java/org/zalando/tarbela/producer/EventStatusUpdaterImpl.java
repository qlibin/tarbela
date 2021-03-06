package org.zalando.tarbela.producer;

import static org.zalando.tarbela.util.StringConstants.CONTENT_TYPE_BUNCH_OF_EVENT_UPDATES;

import java.net.URI;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import org.springframework.web.client.RestOperations;

import org.zalando.tarbela.producer.models.BunchOfEventUpdates;
import org.zalando.tarbela.producer.models.EventUpdate;

public class EventStatusUpdaterImpl implements EventStatusUpdater {

    private static final MediaType UPDATE_MEDIA_TYPE = MediaType.parseMediaType(CONTENT_TYPE_BUNCH_OF_EVENT_UPDATES);
    private final RestOperations template;
    private final URI eventsUri;

    public EventStatusUpdaterImpl(final URI eventsUri, final RestOperations template) {
        this.template = template;
        this.eventsUri = eventsUri;
    }

    @Override
    public void updateStatuses(final List<EventUpdate> updates) {
        final BunchOfEventUpdates bunch = new BunchOfEventUpdates();
        bunch.setEvents(updates);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(UPDATE_MEDIA_TYPE);

        final HttpEntity<BunchOfEventUpdates> requestEntity = new HttpEntity<>(bunch, headers);

        template.exchange(eventsUri, HttpMethod.PATCH, requestEntity, Void.class);
    }

}
