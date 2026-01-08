package com.mwu.aitok.service.video.service;

import com.mwu.aitok.model.video.domain.VideoTag;
import io.netty.resolver.dns.DnsServerAddresses;

import java.util.List;

public interface VideoTagService {
    List<VideoTag> random10VideoTags();
}
