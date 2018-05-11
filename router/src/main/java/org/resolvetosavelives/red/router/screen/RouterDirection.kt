package org.resolvetosavelives.red.router.screen

/**
 * This class exists just to avoid exposing Flow's Direction class to other modules.
 */
enum class Direction(flowDirection: flow.Direction) {
  FORWARD(flow.Direction.FORWARD),
  BACKWARD(flow.Direction.BACKWARD),
  REPLACE(flow.Direction.REPLACE);
}
