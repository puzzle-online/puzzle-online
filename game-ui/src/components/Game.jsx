import React, {useCallback, useState} from "react";
import {Sprite, Stage, Container, useApp} from "@pixi/react";
import {Texture} from "pixi.js";
import cursor from "../assets/cursor.png";

const width = 800;
const height = 500;
const backgroundColor = 0x1d2320;

let index = 1;


function DraggableBox({tint, x = 0, y = 0, cursorPosition, setOnBoxMove, ...props}) {
    const isDragging = React.useRef(false);
    const offset = React.useRef({x: 0, y: 0});
    const [position, setPosition] = React.useState({x, y})
    const [alpha, setAlpha] = React.useState(1);
    const [zIndex, setZIndex] = React.useState(index);

    const onBoxMoveCallback = useCallback((outsideEvent) => {
        const {x, y} = outsideEvent.data.global;
        if (isDragging.current) {
            setPosition({
                x: x - offset.current.x,
                y: y - offset.current.y,
            })
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

function ContainerWrapper() {
    const [cursorPosition, setCursorPosition] = useState({x: 0, y: 0});
    const app = useApp();
    // const onBoxMove = useRef(null);
    const [onBoxMove, setOnBoxMove] = useState(null);

    function movePlayer(e) {
        const {x, y} = e.data.global
        setCursorPosition({x: x, y: y});
        if (onBoxMove) {
            onBoxMove(e);
        }
    }

    return <Container
        sortableChildren={true}
        pointermove={movePlayer}
        eventMode="static"
        hitArea={app.screen}
    >
        <DraggableBox tint={0xff00ff} x={0} setOnBoxMove={setOnBoxMove}/>
        <DraggableBox tint={0x00ffff} x={100} setOnBoxMove={setOnBoxMove}/>
        <DraggableBox tint={0xffffff} x={200} setOnBoxMove={setOnBoxMove}/>
        <DraggableBox tint={0x00ff00} x={300} setOnBoxMove={setOnBoxMove}/>
        <DraggableBox tint={0xffff00} x={400} setOnBoxMove={setOnBoxMove}/>
        <Cursor position={cursorPosition}/>
    </Container>;
}

function Game() {
    return (
        <Stage width={width} height={height} options={{backgroundColor}}>
            <ContainerWrapper/>
        </Stage>
    );
}

export default Game;