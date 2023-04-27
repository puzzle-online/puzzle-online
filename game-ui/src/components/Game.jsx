import React, {useMemo} from "react";
import {Sprite, Stage} from "@pixi/react";
import {BlurFilter} from "pixi.js";
import {Container} from "@mui/joy";

// PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST

const useDrag = ({ x, y }) => {
    const sprite = React.useRef();
    const [isDragging, setIsDragging] = React.useState(false);
    const [position, setPosition] = React.useState({ x, y });

    const onDown = React.useCallback(() => setIsDragging(true), []);
    const onUp = React.useCallback(() => setIsDragging(false), []);
    const onMove = React.useCallback(e => {
        if (isDragging && sprite.current) {
            setPosition(e.data.getLocalPosition(sprite.current.parent));
        }
    }, [isDragging, setPosition]);

    return {
        ref: sprite,
        interactive: true,
        pointerdown: onDown,
        pointerup: onUp,
        pointerupoutside: onUp,
        pointermove: onMove,
        alpha: isDragging ? 0.5 : 1,
        anchor: 0.5,
        position,
    };
};

const DraggableBunny = ({ x = 400, y = 300, ...props }) => {
    const bind = useDrag({ x, y });

    return (
        <Sprite
            image="https://s3-us-west-2.amazonaws.com/s.cdpn.io/693612/IaUrttj.png"
            scale={4}
            {...bind}
            {...props}
        />
    );
}

function Game() {
    const blurFilter = useMemo(() => new BlurFilter(4), []);

    return (
        <Stage>
            <DraggableBunny x={100} scale={2} />
            <DraggableBunny x={300} scale={3} />
            <DraggableBunny x={500} scale={4} />
            <DraggableBunny x={700} scale={5} />
        </Stage>
    );
}

export default Game;