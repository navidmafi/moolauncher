package com.navidmafi.moolauncher.downloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadedDownloader implements IDownloader {

    private final Queue<DownloadJob> queue = new LinkedList<>();
    private final int maxRetries;
    private final int threadCount;
    private final DownloaderListener listener;

    private ExecutorService executor;

    private final AtomicInteger totalFiles = new AtomicInteger(0);
    private final AtomicInteger completedFiles = new AtomicInteger(0);

    public MultiThreadedDownloader(int threadCount, int maxRetries, DownloaderListener listener) {
        if (threadCount <= 0) throw new IllegalArgumentException("threadCount must be > 0");
        if (maxRetries < 0) throw new IllegalArgumentException("maxRetries must be ≥ 0");
        if (listener == null) throw new NullPointerException("listener must not be null");

        this.threadCount = threadCount;
        this.maxRetries = maxRetries;
        this.listener = listener;
    }

    @Override
    public synchronized void add(DownloadJob job) {
        if (job == null) throw new NullPointerException("job must not be null");
        queue.add(job);
    }

    @Override
    public synchronized void start() {
        if (executor != null && !executor.isShutdown()) {
            throw new IllegalStateException("Downloader already started");
        }

        totalFiles.set(queue.size());
        completedFiles.set(0);

        if (totalFiles.get() == 0) {
            listener.onFinished(this);
            return;
        }

        executor = Executors.newFixedThreadPool(threadCount);

        while (!queue.isEmpty()) {
            DownloadJob job = queue.poll();
            executor.submit(() -> {
                try {
                    downloadWithRetries(job);
                } catch (Exception e) {
                    listener.onError(this, e);
                }
            });
        }
        executor.shutdown();
    }

    private void downloadWithRetries(DownloadJob job) {
        int attempts = 0;
        boolean success = false;

        while (attempts <= maxRetries && !success) {
            attempts++;
            job.setState(DownloadState.DOWNLOADING);

            try {
                downloadFile(job);
                job.setState(DownloadState.DOWNLOADED);
                success = true;

                int finishedSoFar = completedFiles.incrementAndGet();
                int percent = (int) ((finishedSoFar / (double) totalFiles.get()) * 100);
                listener.onProgress(this, percent);

            } catch (IOException e) {
                if (attempts > maxRetries) {
                    job.setState(DownloadState.FAILED);
                    listener.onError(this, e);
                }
            }
        }

        if (completedFiles.get() == totalFiles.get()) {
            listener.onFinished(this);
        }
    }

    public static void downloadFile(DownloadJob job) throws IOException {
        URL url = new URL(job.getUrl());

        Files.createDirectories(job.getPath().getParent());

        try (
                BufferedInputStream in = new BufferedInputStream(url.openStream());
                FileOutputStream fileOut = new FileOutputStream(job.getPath().toFile())
        ) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    public void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
