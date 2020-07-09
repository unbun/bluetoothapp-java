### Robot Control Strings

The app controls the robot arm by sending formatted control strings. The formats are as follows:

##### Move Joints: **MOV**:***<jjj>***:[param]
###### ***<jjj>***: 
- _J0_-_J6_ to represent an individual joint to move
- _ALL_ to represent all joints
- _NaN_ to repesent no joints
- [param]
    - w=[angular speed]

##### Stop Joints: **STP**:***<jjj>***
###### ***<jjj>***: 
- _J0_-_J6_ to represent an individual joint to move
- _ALL_ to represent all joints
- _NaN_ to repesent no joints

##### Retract Joints: **RTC**:***<jjj>***
###### ***<jjj>***: 
- _J0_-_J6_ to represent an individual joint to move
- _ALL_ to represent all joints
- _NaN_ to repesent no joints

##### Track Body Parts: **TRK**:***<target>***
###### ***<target>***: 
- _HEAD_, _CHST_, or _HAND_

### Bluetooth connections
https://www.youtube.com/watch?v=y8R2C86BIUc