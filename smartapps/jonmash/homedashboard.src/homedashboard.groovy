/**
 *  HomeDashboard
 *
 *  Copyright 2017 Jonathan Mash
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "HomeDashboard",
    namespace: "jonmash",
    author: "Jonathan Mash",
    description: "No description for now...",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section ("Allow external service to control these things...") {
        input "switches", "capability.switch", multiple: true, required: true
    }
}

mappings {
    path("/list") {
        action: [
            GET: "listSwitches"
        ]
    }
    path("/cmd/:id/:command") {
        action: [
            GET: "updateSwitch"
        ]
    }
}

// returns a list like
// [[name: "kitchen lamp", value: "off"], [name: "bathroom", value: "on"]]
def listSwitches() {
    def resp = []
    switches.each {
        resp << [id: it.id, name: it.displayName, value: it.currentValue("switch")]
    }
    return resp
}

def updateSwitch() {
    // use the built-in request object to get the command parameter
    def command = params.command
    def id = params.id
	log.debug "Running command ${command} on ${id}"

    def size = switches.findAll{it.id == id}.size()
    if(size != 1) {
        httpError(400, "Device (${id}) not found.")
    }
    log.debug "Found: ${size}"
    
    def foundSwitch = switches.find{it.id == id}
    switch(command) {
        case "on":
            foundSwitch.on()
       		break
        case "off":
        	foundSwitch.off()
        	break
        case "toggle":
        	if (foundSwitch.currentSwitch == "on") {
                foundSwitch.off()
            } else {
               foundSwitch.on()
            }
        	break
        case "info":
        	break
        default:
            httpError(400, "$command is not a valid command for all switches specified")
    }
    def resp = []
    resp << [id: foundSwitch.id, name: foundSwitch.displayName, value: foundSwitch.currentValue("switch"), cmd: command]
    return resp
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers