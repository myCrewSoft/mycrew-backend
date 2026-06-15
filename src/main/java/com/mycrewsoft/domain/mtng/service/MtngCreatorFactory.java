package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mtng.enums.MtngTypeCode;
import org.springframework.stereotype.Component;

// [팩토리 메서드 패턴]
// MtngTypeCode(ONLINE/OFFLINE/HYBRID)를 받아서
// 해당 타입에 맞는 AbstractMtngCreator 구현체를 반환한다.
//
// 호출하는 쪽(MtngServiceImpl)은 OnlineMtngCreator, OfflineMtngCreator,
// HybridMtngCreator라는 구체적인 클래스 이름을 전혀 몰라도 된다.
// 새 타입이 추가되면 이 클래스의 switch문에 case 하나만 추가하면 된다.
@Component
public class MtngCreatorFactory {

    private final VideoConfMtngCreator videoConfMtngCreator;
    private final OfflineMtngCreator offlineMtngCreator;
    private final HybridMtngCreator hybridMtngCreator;

    // 같은 부모(AbstractMtngCreator)를 상속한 빈이 여러 개라서
    // @RequiredArgsConstructor 대신 명시적 생성자로 각 구현체를 명확히 주입받는다
    public MtngCreatorFactory(
            VideoConfMtngCreator videoConfMtngCreator,
            OfflineMtngCreator offlineMtngCreator,
            HybridMtngCreator hybridMtngCreator) {
        this.videoConfMtngCreator = videoConfMtngCreator;
        this.offlineMtngCreator = offlineMtngCreator;
        this.hybridMtngCreator = hybridMtngCreator;
    }

    public AbstractMtngCreator getCreator(MtngTypeCode typeCode) {
        switch (typeCode) {
            case ONLINE:
                return videoConfMtngCreator;
            case OFFLINE:
                return offlineMtngCreator;
            case HYBRID:
                return hybridMtngCreator;
            default:
                // MtngTypeCode enum에 새 값이 추가됐는데
                // 여기에 case를 안 추가하면 이 예외로 즉시 알 수 있음
                throw new CustomException(ErrorCode.INVALID_MTNG_TYPE_CD);
        }
    }
}