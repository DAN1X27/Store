package danix.app.Store.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtils {

    private PageUtils() {
    }

    public static Pageable getPage(int page, int count, String property) {
        return PageRequest.of(page, count, getSort(property));
    }

    public static Sort getSort(String property) {
        return Sort.by(Sort.Direction.DESC, property);
    }

}
