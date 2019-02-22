# Burner Wallet android app

#### preview version for livenet @ https://betas.to/UzqbEryQ


### setup

- follow burner wallet dev setup
- connect device through usb/emulator and call `adb reverse tcp:8545 tcp:8545` to bridge to localhost
- shake device to open 'Debug Drawer' or call `adb shell am start com.vander.burner.debug/com.vander.burner.app.ui.DebugActivity`

debug drawer provides endpoints for:
- `http://localhost:8545`
- `https://dai.poa.network`
