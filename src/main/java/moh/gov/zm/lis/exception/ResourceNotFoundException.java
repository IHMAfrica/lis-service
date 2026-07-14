package moh.gov.zm.lis.exception;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s not found with id: %s", resource, id),
                "RESOURCE_NOT_FOUND", 404);
    }
}
