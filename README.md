## Robot Control Strings

The app controls the robot arm by sending formatted control strings. The formats are as follows:

#### Move Joints: '_MOV:[JJJ]:[param]_'
##### [JJJ]:
- _J0_-_J6_ to represent an individual joint to move
- _ALL_ to represent all joints
- _NaN_ to repesent no joints
- [param]
    - w=[angular speed]

#### Stop Joints: '_STP:[JJJ]_'
##### [JJJ]: 
- _J0_-_J6_ to represent an individual joint to move
- _ALL_ to represent all joints
- _NaN_ to repesent no joints

#### Retract Joints: '__RTC:[JJJ]__'
##### [JJJ]: 
- _J0_-_J6_ to represent an individual joint to move
- _ALL_ to represent all joints
- _NaN_ to repesent no joints

#### Track Body Parts: '_TRK**:[target]_'
##### [target]: 
- _HEAD_, _CHST_, or _HAND_

### Bluetooth connections
https://www.youtube.com/watch?v=y8R2C86BIUc
