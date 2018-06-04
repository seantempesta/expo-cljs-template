#!/bin/bash
echo `ip route get 8.8.8.8 | head -n 1 | tr -s ' ' | cut -d ' ' -f 7` > .lan-ip
