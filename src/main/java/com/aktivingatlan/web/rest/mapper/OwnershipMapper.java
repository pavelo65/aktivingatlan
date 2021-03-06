package com.aktivingatlan.web.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.aktivingatlan.domain.Client;
import com.aktivingatlan.domain.Ownership;
import com.aktivingatlan.web.rest.dto.OwnershipDTO;

/**
 * Mapper for the entity Ownership and its DTO OwnershipDTO.
 */
@Mapper(componentModel = "spring", uses = { PhotoMapper.class })
public interface OwnershipMapper {

    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "property.code", target = "propertyCode")
    @Mapping(source = "property.descriptionHu", target = "propertyDescriptionHu")
    @Mapping(source = "property.city.id", target = "propertyCityId")
    @Mapping(source = "property.city.name", target = "propertyCityName")
    @Mapping(source = "property.photos", target = "propertyPhotos")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.name", target = "clientName")
    OwnershipDTO ownershipToOwnershipDTO(Ownership ownership);

    @Mapping(source = "propertyId", target = "property")
    @Mapping(source = "clientId", target = "client")
    Ownership ownershipDTOToOwnership(OwnershipDTO ownershipDTO);

//    default Property propertyFromId(Long id) {
//        if (id == null) {
//            return null;
//        }
//        Property property = new Property();
//        property.setId(id);
//        return property;
//    }

    default Client clientFromId(Long id) {
        if (id == null) {
            return null;
        }
        Client client = new Client();
        client.setId(id);
        return client;
    }
    
}
