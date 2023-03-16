
package net.griddb.springbootblog;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.toshiba.mwcloud.gs.*;
import java.util.List;

/* GET /device/$id -> get device
 * POST /device/$id -> create device
 * GET /devices -> get all devices
 * UPDATE /device/$id -> update device
 * DELETE /device/$id -> delete device
 */

@RestController
public class DeviceController {

    GridDB gridDb;

    public DeviceController() {
        super();
        gridDb = new GridDB();
    }

	@GetMapping("/devices")
	public List<Device> getDevice() {

        return gridDb.getDevices();
	}


	@GetMapping("/device/{id}")
	public ResponseEntity<?> getDevice(@PathVariable String id) {

        Device dev = gridDb.getDevice(id);
        if (dev == null)
            return new ResponseEntity<String>("Device not found\n", HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<Device>(dev, HttpStatus.OK);
	}


    @PutMapping("/device")
    public ResponseEntity<?> putDevice(@RequestBody Device dev) {
        Device dbdev = gridDb.getDevice(dev.getId());

        if (dbdev == null)
            return new ResponseEntity<String>("Device not found\n", HttpStatus.NOT_FOUND);

        if(dev.getLat() != null)
            dbdev.setLat(dev.getLat());
        if(dev.getLon() != null)
            dbdev.setLon(dev.getLon());
        
        gridDb.putDevice(dev);
        return new ResponseEntity<Device>(dbdev, HttpStatus.OK);
    }


    
    @PostMapping("/device")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> postDevice(@RequestBody Device dev) {
        if (gridDb.getDevice(dev.getId()) == null) {
            gridDb.putDevice(dev);
            return new ResponseEntity<Device>(dev, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<String>("Device already exists\n", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/device/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable String id) {
        if (gridDb.getDevice(id) != null) {
            gridDb.deleteDevice(id);
            return new ResponseEntity<String>("device deleted\n", HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<String>("Device does not yet exist. Nothing deleted.\n", HttpStatus.BAD_REQUEST);
        }
    }

}
