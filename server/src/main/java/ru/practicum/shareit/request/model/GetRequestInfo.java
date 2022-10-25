package ru.practicum.shareit.request.model;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetRequestInfo {

    private Long userId;

    private Integer from;

    private Integer size;

    public static GetRequestInfo of(Long userId, Integer from, Integer size) {
        GetRequestInfo getRequestInfo = new GetRequestInfo();
        getRequestInfo.setUserId(userId);
        getRequestInfo.setFrom(from);
        getRequestInfo.setSize(size);

        return getRequestInfo;
    }

}
