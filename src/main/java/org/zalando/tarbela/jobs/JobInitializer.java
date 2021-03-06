package org.zalando.tarbela.jobs;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.TaskScheduler;

import org.springframework.stereotype.Component;

import org.zalando.tarbela.config.util.ProducerInteractor;
import org.zalando.tarbela.config.util.ProducerInteractorContainer;
import org.zalando.tarbela.nakadi.NakadiClient;
import org.zalando.tarbela.service.EventService;

import org.zalando.tracer.Tracer;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JobInitializer implements InitializingBean {
    private static final int JOB_START_OFFSET_SECONDS = 5;
    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ProducerInteractorContainer producerInteractors;

    @Autowired
    private NakadiClient nakadiClient;

    @Autowired
    private Tracer tracer;
    
    private Instant lastJobStart = Instant.now();

    @Override
    public void afterPropertiesSet() throws Exception {
        producerInteractors.getProducerInteractors().forEach(producerInteractor -> startJob(producerInteractor));
    }

    private void startJob(final ProducerInteractor interactor) {
        log.info("Starting job for producer: {}", interactor.getProducerName());

        final EventService eventService = new EventService(interactor.getEventRetriever(),
                interactor.getEventStatusUpdater(), nakadiClient);
        taskScheduler.scheduleWithFixedDelay(() -> {
                tracer.start();
                try {
                    log.info("Running job for producer: {}", interactor.getProducerName());
                    eventService.publishEvents();
                } finally {
                    tracer.stop();
                }
            },
            Date.from(lastJobStart),
            interactor.getJobInterval());
        lastJobStart = lastJobStart.plusSeconds(JOB_START_OFFSET_SECONDS);
    }
}
