import React, {useCallback, useEffect, useRef, useState} from "react";
import {Sprite, Stage, Container, useApp} from "@pixi/react";
import {Texture} from "pixi.js";
import cursor from "../assets/cursor.png";

const width = 1200;
const height = 800;
const backgroundColor = 0x505050;

let index = 1;


function DraggableBox({tint, x = 0, y = 0, cursorPosition, setOnBoxMove, boxId, ...props}) {
    const isDragging = React.useRef(false);
    const offset = React.useRef({x: 0, y: 0});
    const [position, setPosition] = React.useState({x, y})
    const [alpha, setAlpha] = React.useState(1);
    const [zIndex, setZIndex] = React.useState(index);

    const onBoxMoveCallback = useCallback((outsideEvent) => {
        const {x, y} = outsideEvent.data.global;
        if (isDragging.current) {
            let newX = x - offset.current.x;
            let newY = y - offset.current.y;
            setPosition({
                x: newX,
                y: newY,
            })
            return {boxId: boxId, boxX: newX, boxY: newY};
        }
    }, []);

    function onStart(e) {
        const {x, y} = e.data.global;

        isDragging.current = true;

        offset.current = {
            x: x - position.x,
            y: y - position.y
        };

        setAlpha(0.5);
        setZIndex(index++);

        setOnBoxMove(() => onBoxMoveCallback);
    }

    function onEnd() {
        isDragging.current = false;
        setOnBoxMove(null);
        setAlpha(1);
    }

    return (
        <Sprite
            {...props}
            alpha={alpha}
            position={position}
            texture={Texture.WHITE}
            width={100}
            height={100}
            zIndex={zIndex}
            tint={tint}
            eventMode='static'
            pointerdown={onStart}
            pointerup={onEnd}
            pointerupoutside={onEnd}
        />
    );
}

function Cursor({position}) {
    return (
        <Sprite
            position={position}
            texture={Texture.from(cursor)}
            width={30}
            height={30}
            eventMode='none'
            zIndex={9999}
        />
    );
}

function ContainerWrapper({sendRequest, roomId, boxes, clients}) {
    const [cursorPosition, setCursorPosition] = useState({x: 0, y: 0});
    const app = useApp();
    // const onBoxMove = useRef(null);
    const [onBoxMove, setOnBoxMove] = useState(null);

    function movePlayer(e) {
        const {x, y} = e.data.global
        setCursorPosition({x: x, y: y});
        // TODO: use backlog of events instead of sending every event
        const move = {cursor: {x: x, y: y}, roomId: roomId};
        move.box = null;
        if (onBoxMove) {
            const {boxId, boxX, boxY} = onBoxMove(e);
            move.box = {id: boxId, x: boxX, y: boxY};
        }
        sendRequest('move', move);
    }

    return <Container
        sortableChildren={true}
        pointermove={movePlayer}
        eventMode="static"
        hitArea={app.screen}
    >
        {/*<DraggableBox tint={0xff00ff} x={0} y={0} setOnBoxMove={setOnBoxMove}/>*/}
        {/*<DraggableBox tint={0x00ffff} x={100} y={0} setOnBoxMove={setOnBoxMove}/>*/}
        {/*<DraggableBox tint={0xf0f0f0} x={200} y={0} setOnBoxMove={setOnBoxMove}/>*/}
        {/*<DraggableBox tint={0x00ff00} x={300} y={0} setOnBoxMove={setOnBoxMove}/>*/}
        {/*<DraggableBox tint={0xffff00} x={400} y={0} setOnBoxMove={setOnBoxMove}/>*/}
        {/*<DraggableBox tint={0x0000ff} x={500} y={0} setOnBoxMove={setOnBoxMove}/>*/}
        {/*<DraggableBox tint={0xff0000} x={600} y={0} setOnBoxMove={setOnBoxMove}/>*/}
        {/*<DraggableBox tint={0x000000} x={700} y={0} setOnBoxMove={setOnBoxMove}/>*/}
        {boxes.map((box) => {
            return <DraggableBox key={box.id} tint={0xff00ff} x={box.x} y={box.y} setOnBoxMove={setOnBoxMove} boxId={box.id}/>
        })}
        {/*<Cursor position={cursorPosition}/>*/}
        {clients.map((client) => {
            return <Cursor key={client.id} position={client.cursor}/>
        })}
        <Sprite position={{x: 400, y: 200}} texture={Texture.WHITE} width={400} height={400} zIndex={-1}/>
    </Container>;
}

function Game({sendRequest, roomId, boxes, clients}) {
    return (
        <Stage width={width} height={height} options={{backgroundColor}}>
            <ContainerWrapper sendRequest={sendRequest} roomId={roomId} boxes={boxes} clients={clients}/>
        </Stage>
    );
}

export default Game;