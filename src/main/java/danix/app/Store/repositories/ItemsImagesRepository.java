package danix.app.Store.repositories;

import danix.app.Store.models.Item;
import danix.app.Store.models.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemsImagesRepository extends JpaRepository<ItemImage, Long> {
}
